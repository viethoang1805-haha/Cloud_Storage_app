#!/bin/bash
# Script này chạy sau khi MinIO khởi động
# Dùng mc (MinIO Client) để tạo bucket mặc định

sleep 5
mc alias set local http://localhost:9000 minioadmin minioadmin123
mc mb local/cloud-storage-bucket
mc anonymous set download local/cloud-storage-bucket
echo "MinIO bucket created successfully"