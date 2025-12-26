# Food Service

[**← Back to Main Architecture**](https://github.com/oleh-prukhnytskyi/macro-tracker)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white)
![ElasticSearch](https://img.shields.io/badge/-ElasticSearch-005571?style=for-the-badge&logo=elasticsearch)
![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazon-s3&logoColor=white)
![Google Gemini](https://img.shields.io/badge/Google%20Gemini-8E75B2?style=for-the-badge&logo=googlebard&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

---

[![License](https://img.shields.io/badge/license-Apache%202.0-blue?style=for-the-badge)](LICENSE)
[![Swagger](https://img.shields.io/badge/Swagger-API_Docs-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://macrotracker.uk/webjars/swagger-ui/index.html?urls.primaryName=food-service)
[![Docker Hub](https://img.shields.io/badge/Docker%20Hub-Image-blue?style=for-the-badge&logo=docker)](https://hub.docker.com/repository/docker/olehprukhnytskyi/macro-tracker-food-service/general)

**Product Catalog, Image Management & Intelligent Search Service.**

Responsible for managing the food database, processing product images, and providing high-performance fuzzy search capabilities enriched by AI.

## :zap: Service Specifics

* **Polyglot Persistence**:
    * **MongoDB**: Primary store for unstructured food product data and nutritional info.
    * **Elasticsearch**: Used for high-speed fuzzy search, autocomplete suggestions, and barcode lookups.
    * **PostgreSQL**: Handles the `OutboxEvent` table for reliable eventual consistency.
* **AI Enrichment**: Integrates **Google Gemini API** to automatically generate descriptive keywords for new products, enhancing search discoverability.
* **Asset Management**: Handles image uploads to **AWS S3** with on-the-fly resizing and optimization using `Thumbnailator`.
* **Resilience**:
    * **Retry Mechanism**: Automatic retries for optimistic locking failures (`DuplicateKeyException`) using Spring Retry.
    * **Fault Tolerance**: Robust error handling for external AI and Storage services.

---

## :electric_plug: API & Communication

* **Public API**: Endpoints for searching foods, getting suggestions, and CRUD operations on products.
* **Internal Communication**:
    * *Async Processing*: Uses Spring Events (`FoodCreatedEvent`) to trigger background AI keyword generation without blocking the HTTP response.
    * *Event Consistency*: Writes `FOOD_DELETED` events to the **Outbox** table. A background job (`OutboxJob`) processes these events to asynchronously clean up associated images from S3.

---

## :hammer_and_wrench: Tech Details

| Component | Implementation |
| :--- | :--- |
| **Search Engine** | Elasticsearch (Fuzzy, n-grams, Barcode support) |
| **Database** | MongoDB (Data), PostgreSQL (Outbox) |
| **Storage** | AWS S3 (via AWS SDK v2) |
| **AI / ML** | Google Gemini 2.5 Flash |
| **Caching** | Redis (`search:results`, `food:data`) |
| **Locking** | ShedLock (ensures single-instance image cleanup jobs) |

---

## :gear: Environment Variables

Required variables for `local` or `k8s` deployment:

| Variable | Purpose |
| :--- | :--- |
| **Databases** | |
| `MONGODB_HOST`, `MONGODB_PORT` | MongoDB connection details. |
| `MONGODB_NAME`, `MONGODB_PASSWORD` | MongoDB credentials. |
| `DB_HOST`, `DB_PORT` | PostgreSQL connection details (Outbox). |
| `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL credentials. |
| **Search & Cache** | |
| `ELASTICSEARCH_URIS` | Address of the Elasticsearch cluster. |
| `ELASTICSEARCH_USERNAME` | Elasticsearch user. |
| `ELASTICSEARCH_PASSWORD` | Elasticsearch password. |
| `REDIS_URL` | Redis connection URL. |
| **AI Integration** | |
| `GEMINI_API_KEY` | Google AI Studio API Key for keyword generation. |
| **Object Storage (AWS S3)** | |
| `AWS_S3_REGION` | AWS Region (e.g., `us-east-1`). |
| `AWS_S3_BUCKET` | S3 Bucket name for food images. |
| `AWS_S3_ACCESS_KEY` | IAM Access Key. |
| `AWS_S3_SECRET_KEY` | IAM Secret Key. |
| **General** | |
| `MACRO_TRACKER_URL` | Public URL of the application. |

---

## :whale: Quick Start

```bash
# Pull from Docker Hub
docker pull olehprukhnytskyi/macro-tracker-food-service:latest

# Run (Ensure your .env file contains all required variables listed above)
docker run -p 8080:8080 --env-file .env olehprukhnytskyi/macro-tracker-food-service:latest
```

---

## :balance_scale: License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.