# Copilot / AI Agent Instructions

목적: 이 파일은 AI 코딩 에이전트가 이 저장소에서 즉시 생산적으로 작업할 수 있도록, 아키텍처 핵심, 개발 워크플로, 프로젝트 고유 규약, 통합 포인트를 간결하게 정리합니다.

요약(한줄): Spring Boot 애플리케이션 + Spring Data Elasticsearch (ELC) 기반 검색 서비스. 인덱스 매핑과 분석기 설정은 리소스의 정적 파일을 사용합니다.

주요 구성요소 및 책임
- **컨트롤러**: HTTP 엔드포인트 노출
  - [src/main/java/com/example/elastic/controller/ElasticStoresController.java](src/main/java/com/example/elastic/controller/ElasticStoresController.java#L1)
  - 경로: `api/v2/elastic` — 검색, 저장, 삭제, 분석 엔드포인트 제공
- **서비스**: 비즈니스 로직 + Elasticsearch 쿼리 작성
  - [src/main/java/com/example/elastic/service/ElasticStoresImpl.java](src/main/java/com/example/elastic/service/ElasticStoresImpl.java#L1)
  - `ElasticAnalyzeService`는 `_analyze` API 호출을 위해 `WebClient` 사용 ([src/main/java/com/example/elastic/service/ElasticAnalyzeService.java](src/main/java/com/example/elastic/service/ElasticAnalyzeService.java#L1))
- **리포지토리 / 데이터 모델**
  - 도메인: [src/main/java/com/example/elastic/domain/entity/ElasticStores.java](src/main/java/com/example/elastic/domain/entity/ElasticStores.java#L1) — `@Document`, `@Mapping`, `@Setting` 사용
  - 리포지토리: [src/main/java/com/example/elastic/repository/ElasticStoresRepository.java](src/main/java/com/example/elastic/repository/ElasticStoresRepository.java#L1)
- **설정**
  - Elasticsearch 연결 및 커스텀 변환: [src/main/java/com/example/elastic/domain/config/ElasticConfig.java](src/main/java/com/example/elastic/domain/config/ElasticConfig.java#L1)
  - CORS: [src/main/java/com/example/elastic/domain/config/WebConfig.java](src/main/java/com/example/elastic/domain/config/WebConfig.java#L1)

핵심 패턴과 프로젝트 규약 (구체적)
- 인덱스 설정/매핑 파일은 리소스의 정적 JSON 사용
  - [src/main/resources/static/elastic/elastic-settings.json](src/main/resources/static/elastic/elastic-settings.json#L1)
  - [src/main/resources/static/elastic/stores-mappings.json](src/main/resources/static/elastic/stores-mappings.json#L1)
  - 엔티티에 `@Setting(settingPath=...)`와 `@Mapping(mappingPath=...)`로 연결됨 (`ElasticStores`)
- Spring Data Elasticsearch ELC API 사용
  - 고급 쿼리는 `NativeQuery` 빌더와 `ElasticsearchOperations`를 통해 작성됨 (`ElasticStoresImpl`) — ELC 전용 DSL 스타일을 따름
- 직렬화/역직렬화 커스텀 컨버터
  - `ElasticConfig`에서 `ElasticsearchCustomConversions`로 `ElasticStores <-> Map` 변환 구현 — 저장/검색에 영향을 줌
- DTO 방식
  - 요청은 Java `record`인 `ElasticInformation`을 사용하고, 엔티티 변환은 `toEntity` 정적 메서드로 통일

운영 / 개발 워크플로 (실행 가능한 명령)
- 로컬 빌드 / 실행
  - 빌드: `./gradlew build`
  - 앱 실행(개발): `./gradlew bootRun`
- Elasticsearch 의존성
  - 기본 `application.yml`은 `spring.elasticsearch.rest.uris: http://elastic:9200`로 설정되어 있음 ([src/main/resources/application.yml](src/main/resources/application.yml#L1))
  - 도커/로컬에서 실행할 때 서비스 이름이 `elastic`이거나 환경변수로 오버라이드 필요 (예: `SPRING_ELASTICSEARCH_REST_URIS=http://localhost:9200`)
- 분석 API 호출 주의사항
  - `ElasticAnalyzeService`는 `_analyze` 호출을 `http://localhost:9200/_analyze`로 고정하고 있음 — 컨테이너 환경에서는 `application.yml`의 호스트(`elastic`)와 불일치할 수 있으니 환경에 맞게 `WebClient` 호출 대상이나 환경변수를 사용하도록 수정 권장

디버깅 팁
- 검색 동작을 재현하려면 `ElasticStoresImpl`의 `NativeQuery` 구성을 중심으로 테스트 케이스를 만드세요. 검색 결과는 `elasticsearchOperations.search(...)`의 `SearchHits`에서 추출됩니다.
- 매핑/분석기 문제는 인덱스 생성 시 `elastic-settings.json`과 `stores-mappings.json`을 먼저 적용해서 확인하세요.

변경 시 체크리스트 (PR 작성 시 유의)
- 매핑 파일을 변경하면 기존 인덱스와 충돌할 수 있으므로 인덱스 재생성/마이그레이션 필요성 명시
- `ElasticAnalyzeService`의 하드코딩된 URL을 환경변수로 대체하는 변경은 실행 환경(로컬 vs 컨테이너) 테스트 포함

참고 파일
- 컨트롤러: [src/main/java/com/example/elastic/controller/ElasticStoresController.java](src/main/java/com/example/elastic/controller/ElasticStoresController.java#L1)
- 서비스 구현: [src/main/java/com/example/elastic/service/ElasticStoresImpl.java](src/main/java/com/example/elastic/service/ElasticStoresImpl.java#L1)
- 분석 서비스: [src/main/java/com/example/elastic/service/ElasticAnalyzeService.java](src/main/java/com/example/elastic/service/ElasticAnalyzeService.java#L1)
- 도메인: [src/main/java/com/example/elastic/domain/entity/ElasticStores.java](src/main/java/com/example/elastic/domain/entity/ElasticStores.java#L1)
- 설정: [src/main/java/com/example/elastic/domain/config/ElasticConfig.java](src/main/java/com/example/elastic/domain/config/ElasticConfig.java#L1)
- 리소스 매핑/설정: [src/main/resources/static/elastic/elastic-settings.json](src/main/resources/static/elastic/elastic-settings.json#L1), [src/main/resources/static/elastic/stores-mappings.json](src/main/resources/static/elastic/stores-mappings.json#L1)

추가 힌트/제안(선택적)
- `ElasticAnalyzeService`가 `spring.elasticsearch.rest.uris`를 재사용하도록 구성하면 배포 환경 차이를 줄일 수 있습니다.
- 테스트: 간단한 통합 테스트는 실제 ES 대신 Testcontainers(또는 로컬 ES)로 인덱스를 만들고 `ElasticsearchOperations` 동작을 검증하세요.

피드백 요청: 이 파일에서 더 설명이 필요하거나, 예시 코드(예: 작은 통합 테스트) 추가를 원하면 알려주세요.
