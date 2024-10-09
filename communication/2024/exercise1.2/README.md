# Quick Start Guide for gRPC Setup

This guide provides instructions on how to generate gRPC code from `.proto` files and run both the server and client.

## Project Structure

## Step 1: Install Dependencies

```bash
pip install grpcio grpcio-tools
pip install protobuf==5.27.2
```

## Step 2: Generate grpc Python code and run the server
```bash
cd pyServer
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. hash.proto
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. data.proto
python server.py
```


## Step 3: Run the client
```bash
cd ..
cd pyClient
python client.py
```