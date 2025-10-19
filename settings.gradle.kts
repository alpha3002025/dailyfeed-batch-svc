rootProject.name = "dailyfeed-batch-svc"

include(
    "dailyfeed-code",
    "dailyfeed-redis-support",
    "dailyfeed-kafka-support",
    "dailyfeed-pvc-support",
    "dailyfeed-batch",
)
