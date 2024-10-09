import grpc

from pyServerr import data_pb2, data_pb2_grpc, hash_pb2, hash_pb2_grpc


def run():
    # Connect to the Data Server
    channel = grpc.insecure_channel('46.249.101.244:50051')
    stub = data_pb2_grpc.DBStub(channel)

    # Register a new user
    username = "arber.hyseni"
    password = "test-unibas"
    register_response = stub.RegisterUser(data_pb2.RegisterUser(username=username, password=password))
    if register_response.success:
        print("User registered successfully")
    elif not register_response.success:
        print("Erorr while registering user")

    data = "Hello from Planet Mars"
    store_response = stub.StoreData(data_pb2.StoreData(username=username, password=password, msg=data))
    if store_response.success:
        print("Data stored successfully")
    elif not store_response.success:
        print("Erorr while storing data")

    # Generate passcode
    passcode_response = stub.GenPasscode(data_pb2.UserPass(username=username, password=password))
    passcode = passcode_response.password

    # connect to hash server
    hash_server = grpc.insecure_channel('localhost:50052')
    hash_stub = hash_pb2_grpc.HSStub(hash_server)

    hash_response = hash_stub.GetHash(hash_pb2.Request(passcode=passcode, ip='localhost', port='50052'))
    generated_hash = hash_response.hash
    print(generated_hash)


if __name__ == "__main__":
    run()
