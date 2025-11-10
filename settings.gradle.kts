rootProject.name = "dailyfeed-batch-svc"

include(
    "dailyfeed-code",
    "dailyfeed-redis-support",
    "dailyfeed-kafka-support",
    "dailyfeed-deadletter-support",
    "dailyfeed-batch",
)
