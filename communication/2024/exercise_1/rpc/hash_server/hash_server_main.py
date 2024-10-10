import grpc
from concurrent import futures
import hashlib

from hash_server import hash_pb2_grpc, hash_pb2, data_pb2_grpc


class HashService(hash_pb2_grpc.HSServicer):
    def GetHash(self, request, context):
        # Connect to the Data Server using gRPC
        data_channel = grpc.insecure_channel(f'{request.ip}:{request.port}')
        data_stub = data_pb2_grpc.DBStub(data_channel)

        # Retrieve the data using the GetAuthData service
        try:
            data_response = data_stub.GetAuthData(f'{request.passcode}')
            print('DATA_RESPONSE_PRINT', data_response)
            user_data = data_response.msg

            # Compute the hash of the retrieved data
            data_hash = hashlib.sha256(user_data.encode()).hexdigest()
            return hash_pb2.Response(hash=data_hash)

        except grpc.RpcError as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details('Error retrieving data from the Data Server')
            return hash_pb2.Response(hash="")


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    hash_pb2_grpc.add_HSServicer_to_server(HashService(), server)
    server.add_insecure_port('[::]:50052')
    server.start()
    print("Hash Server is running on port 50052")
    server.wait_for_termination()


if __name__ == "__main__":
    serve()