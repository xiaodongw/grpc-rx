# GRPC-RX
This is a GRPC stub for RxJava2.

Please be aware this library is still in draft stage, there will be bugs, issues, and API may change in future.

## Getting started
* Clone the repository and run:

    ```
    ./gradlew install
    ``` 
* In your project, configure Gradle to use the grpc-rx compiler plugin

    ```groovy
    protobuf {
      protoc {
        artifact = "com.google.protobuf:protoc:3.1.0"
      }
      plugins {
        grpc {
          artifact = "io.atomos:protoc-gen-grpc-rxjava:0.1.0-SNAPSHOT"
        }
      }
      generateProtoTasks {
        all()*.plugins {
          grpc {}
        }
      }
    }
    ```
    
Now you can fully utilize the elegant API from RxJava2 to handle you requests, for example:

* Client side
    ```java
    EchoGrpcRx.EchoStub stub = EchoGrpcRx.newStub(channel);
    
    Flowable<EchoReq> requests = Flowable.range(0, 10)
        .map(i -> EchoService.EchoReq.newBuilder()
            .setValue("hello")
            .build());

    Subscriber<EchoResp> responseSubscriber = new AutoTestSubscriber(4);

    stub.bidiStreaming(requests)
        .subscribe(responseSubscriber);
    ```
    
* Server side
    ```java
    public Flowable<EchoResp> bidiStreaming(Flowable<EchoReq> requests) {
      return requests.map(r -> EchoResp.newBuilder()
          .setValue("world")
          .build());
    }
    ```
    
## Flow Control
For messages from GRPC to Flowable/Subscriber (responses at client side, requests at server side), 
the flow is controlled by Subscriber with Subscription.request(n), this follows the ReactiveStream pattern well.
 
For messages from Flowable/Publisher to GRPC (requests at client side, responses at server side),
the flow is controlled by watermark settings, 
which means the GRPC subscriber will call Subscription.request(n) to request messages when outstanding message number is less than low watermark.
The outstanding message number will never be more than high watermark.
