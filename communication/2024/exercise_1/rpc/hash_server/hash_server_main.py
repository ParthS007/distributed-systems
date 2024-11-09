import grpc
from concurrent import futures
import hashlib

import hash_pb2_grpc, hash_pb2, data_pb2_grpc


class HashService(hash_pb2_grpc.HSServicer):
    def GetHash(self, request, context):
        try:
            # Generate the hash value of the passcode
            ## TODO: CORRECT THE CODE BELOW
            data_hash = hashlib.sha256(request.passcode.encode()).hexdigest()
            return hash_pb2.Response(hash=data_hash)

        except grpc.RpcError as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details('Error retrieving data from the Data Server')
            return hash_pb2.Response(hash="")


def serve():
    # initiate the server where the rpc's can be serviced
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    hash_pb2_grpc.add_HSServicer_to_server(HashService(), server)
    server.add_insecure_port('[::]:50052')
    server.start()
    print("Hash Server is running on port 50052")
    server.wait_for_termination()


if __name__ == "__main__":
    serve()