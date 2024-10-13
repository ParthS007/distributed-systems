# Remote Procedure Call

### Manual for Running the Hash Server and Client
This guide provides instructions on how to generate gRPC code from `.proto` files and run both the server and client.

#### Prerequisites
`python -m venv venv`\
`source myenv/bin/activate`

- Python 3.x installed on the system.
- Install gRPC dependencies using:
- `pip` => `pip install grpcio grpcio-tools`
- `pip` => `pip install protobuf==5.27.2`

#### Folder Structure
- `client`: The _Client_ folder
- `hash_server`: The _Hash Server_ folder

#### Hash Server Running Instructions

**_Very Important_: Ensure that you are in `rpc` folder.**

1. First we will gRPC code from `.proto` file.
    - `cd hash_server`
    - `python -m grpc_tools.protoc -I. --python_out=./ --grpc_python_out=./ hash.proto`
    - `python -m grpc_tools.protoc -I. --python_out=./ --grpc_python_out=./ data.proto`
    - `cd ..`

2. Run the server - `python -m hash_server.hash_server_main`

#### Client Running Instructions
**_Very Important_: Ensure that you are in `rpc` folder.**

1. Run the client - `python -m client.client_main`
