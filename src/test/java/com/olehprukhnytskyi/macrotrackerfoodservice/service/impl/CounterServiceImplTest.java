package com.olehprukhnytskyi.macrotrackerfoodservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.olehprukhnytskyi.macrotrackerfoodservice.exception.InternalServerErrorException;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Counter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ExtendWith(MockitoExtension.class)
class CounterServiceImplTest {
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private CounterServiceImpl counterService;

    @Test
    @DisplayName("When counter exists, should return next sequence")
    void getNextSequence_whenCounterExists_shouldReturnNextSequence() {
        // Given
        String sequenceName = "test_sequence";
        Counter mockCounter = new Counter();
        mockCounter.setSequence(5L);

        when(mongoTemplate.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                ArgumentMatchers.eq(Counter.class)
        )).thenReturn(mockCounter);

        // When
        Long result = counterService.getNextSequence(sequenceName);

        // Then
        assertEquals(5L, result);
        verify(mongoTemplate).findAndModify(
                any(),
                any(),
                any(),
                ArgumentMatchers.eq(Counter.class)
        );
    }

    @Test
    @DisplayName("When counter is null, should return one")
    void getNextSequence_whenCounterIsNull_shouldReturnOne() {
        // Given
        when(mongoTemplate.findAndModify(
                any(),
                any(),
                any(),
                ArgumentMatchers.eq(Counter.class))
        ).thenReturn(null);

        // When
        Long result = counterService.getNextSequence("test_sequence");

        // Then
        assertEquals(1L, result);
    }

    @Test
    @DisplayName("When sequence is null, should return one")
    void getNextSequence_whenSequenceIsNull_shouldReturnOne() {
        // Given
        Counter counter = new Counter();
        when(mongoTemplate.findAndModify(
                any(), any(), any(), ArgumentMatchers.eq(Counter.class)))
                .thenReturn(counter);

        // When
        Long result = counterService.getNextSequence("seq");

        // Then
        assertEquals(1L, result);
    }

    @Test
    @DisplayName("When Mongo throws exception, should throw InternalServerErrorException")
    void getNextSequence_whenMongoThrowsException_shouldThrowInternalServerErrorException() {
        // Given
        String sequenceName = "test-sequence";
        RuntimeException mongoException = new RuntimeException("Mongo failure");

        when(mongoTemplate.findAndModify(
                any(), any(), any(), ArgumentMatchers.eq(Counter.class)))
                .thenThrow(mongoException);

        // When
        InternalServerErrorException exception = assertThrows(
                InternalServerErrorException.class,
                () -> counterService.getNextSequence(sequenceName)
        );

        // Then
        assertTrue(exception.getMessage().contains("Failed to get next sequence for: "
                + sequenceName));
        assertSame(mongoException, exception.getCause());
    }
}
