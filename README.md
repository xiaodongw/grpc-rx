# GRPC-RX
This is a GRPC stub & compiler for RxJava2.

Check [GRPC-KT](https://github.com/xiaodongw/grpc-kt) for Kotlin Coroutine binding.

## Getting started
  
* In your project, configure Gradle to use the grpc-rxjava stub and compiler plugin

```groovy
dependencies {
  compile "com.github.xiaodongw:grpc-rx-stub:0.5.0"
}
protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:3.5.1"
  }
  plugins {
    grpc {
      artifact = "com.github.xiaodongw:protoc-gen-grpc-rx:0.5.0"
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

## Publish
Publish to Maven Central

* Publish to Sonatype
```
gradle clean :grpc-rx-compiler:uploadArchives -PtargetOs=linux -PtargetArch=x86_64
gradle clean :grpc-rx-compiler:uploadArchives -PtargetOs=windows -PtargetArch=x86_64
gradle clean :grpc-rx-compiler:uploadArchives -PtargetOs=osx -PtargetArch=x86_64
gradle :grpc-rx-stub:uploadArchives
```

* Promote to Maven Central
  * Go to https://oss.sonatype.org/
  * Close the staging repository if there is no problem.
  * Release the repository if close succeeded.
