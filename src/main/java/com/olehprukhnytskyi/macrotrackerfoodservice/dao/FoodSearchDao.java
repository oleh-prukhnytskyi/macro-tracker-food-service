package com.olehprukhnytskyi.macrotrackerfoodservice.dao;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.olehprukhnytskyi.exception.BadRequestException;
import com.olehprukhnytskyi.exception.InternalServerException;
import com.olehprukhnytskyi.exception.error.CommonErrorCode;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FoodSearchDao {
    private final ElasticsearchClient elasticsearchClient;

    public List<Food> search(String query, int offset, int limit) {
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException(CommonErrorCode.BAD_REQUEST,
                    "Query must not be null or empty");
        }
        try {
            Query searchQuery = buildSearchQuery(query);
            SearchResponse<Food> response = elasticsearchClient.search(
                    s -> s.index("macro_tracker.foods")
                            .query(searchQuery)
                            .from(offset)
                            .size(limit),
                    Food.class
            );
            if (response == null || response.hits() == null || response.hits().hits() == null) {
                return Collections.emptyList();
            }
            return response.hits().hits().stream()
                    .map(hit -> {
                        if (hit.source() == null) {
                            return null;
                        }
                        hit.source().setId(hit.id());
                        return hit.source();
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            throw new InternalServerException(CommonErrorCode.INTERNAL_ERROR,
                    "Failed to execute search request", e);
        } catch (Exception e) {
            throw new InternalServerException(CommonErrorCode.INTERNAL_ERROR,
                    "Unexpected error during search", e);
        }
    }

    public List<String> getSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String normalized = query.trim().toLowerCase();
        try {
            SearchResponse<Food> response = elasticsearchClient.search(
                    s -> s.index("macro_tracker.foods")
                            .query(buildSuggestionQuery(normalized)),
                    Food.class
            );
            if (response == null || response.hits() == null || response.hits().hits() == null) {
                return Collections.emptyList();
            }
            return response.hits().hits().stream()
                    .map(hit -> hit.source() != null ? hit.source().getProductName() : null)
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(16)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new InternalServerException(CommonErrorCode.INTERNAL_ERROR,
                    "Failed to fetch search suggestions from Elasticsearch", e);
        }
    }

    private Query buildSearchQuery(String query) {
        String normalizedQuery = query.trim().toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}\\s]", "");
        String[] tokens = normalizedQuery.split("\\s+");
        return Query.of(q -> q.bool(b -> {
            for (String token : tokens) {
                String fuzziness = token.length() > 3 ? "AUTO" : "2";
                b.should(s -> s.multiMatch(mm -> mm
                        .fields("product_name^4", "_keywords^3",
                                "generic_name^2", "brands^2")
                        .query(token)
                        .fuzziness(fuzziness)
                ));
                if (token.matches("\\d{8}|\\d{12}|\\d{13}|\\d{24}")) {
                    String tokenNoZeros = token.replaceFirst("^0+(?!$)", "");
                    processBarcode(b, tokenNoZeros);
                }
            }
            b.minimumShouldMatch("1");
            return b;
        }));
    }

    private Query buildSuggestionQuery(String normalized) {
        return Query.of(q -> q.bool(b -> b
                .should(s1 -> s1.matchPhrase(mp -> mp
                        .field("product_name")
                        .query(normalized)
                        .boost(10f)))
                .should(s2 -> s2.multiMatch(m -> m
                        .fields("product_name",
                                "product_name._2gram",
                                "product_name._3gram")
                        .query(normalized)
                        .type(TextQueryType.BoolPrefix)
                        .boost(4f)))
                .should(s3 -> s3.match(m -> m
                        .field("product_name_ngram")
                        .query(normalized)
                        .fuzziness("AUTO")
                        .boost(2f)))
                .minimumShouldMatch("1")
        ));
    }

    private void processBarcode(BoolQuery.Builder b, String tokenNoZeros) {
        String tokenAsEan13 = tokenNoZeros.length() <= 13
                ? String.format("%013d", Long.parseLong(tokenNoZeros))
                : tokenNoZeros;
        b.should(s -> s.term(t -> t
                .field("code")
                .value(tokenAsEan13)
                .boost(5f)
        ));
        String tokenAsEan8 = tokenNoZeros.length() <= 8
                ? String.format("%08d", Long.parseLong(tokenNoZeros))
                : tokenNoZeros;
        b.should(s -> s.term(t -> t
                .field("code")
                .value(tokenAsEan8)
                .boost(5f)
        ));
        String tokenAsUpc = tokenNoZeros.length() <= 12
                ? String.format("%012d", Long.parseLong(tokenNoZeros))
                : tokenNoZeros;
        b.should(s -> s.term(t -> t
                .field("code")
                .value(tokenAsUpc)
                .boost(5f)
        ));
        String tokenAsEan24 = tokenNoZeros.length() <= 24
                ? String.format("%024d", Long.parseLong(tokenNoZeros))
                : tokenNoZeros;
        b.should(s -> s.term(t -> t
                .field("code")
                .value(tokenAsEan24)
                .boost(5f)
        ));
    }
}
