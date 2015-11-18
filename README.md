# ggamtalk

병렬처리 SNS 서비스인 껨톡의 서버 소스.

Vert.x 서버 프레임워크를 사용하였다. 

>Vert.x

>네트워크 IO 서버인 Netty와 IMDG 인 Hazelcast라는 검증된 엔진 위에서 개발

>Polyglot ( Java, Javascript, Python, Groovy, Scala 지원)

>Verticle이라는 단일 스레드 형태로 작동하여 멀티 스레드에서 발생할 수 있는 동기화 문제 제거

>Event Bus를 통해 클러스터링 설계까지 고려한 서버에 최적화된 프레임워크

>Event Bus를 이용한 Pub/Sub 같은 MQ 기능을 사용 가능


껨톡은 GCM, Chat, DB, Redis, RestfulAPI을 하나의 버티클 단위로 나누어 구현하였으며,

EventBus를 통해 이벤트를 공유하여 작동한다.

