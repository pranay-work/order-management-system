package com.example.oms.grpc;

import io.grpc.Server;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
public class GrpcServerConfig {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerConfig.class);

    private final Server server;

    public GrpcServerConfig(GrpcOrderService grpcOrderService,
                            @Value("${grpc.server.port:9090}") int port) throws IOException {
        this.server = NettyServerBuilder.forPort(port)
                .addService(grpcOrderService)
                .addService(ProtoReflectionService.newInstance())  // Enable reflection for grpcurl
                .build()
                .start();
        log.info("gRPC server started on port {} with reflection enabled", port);
    }

    @PreDestroy
    public void onDestroy() throws InterruptedException {
        shutdown();
    }

    @EventListener(ContextClosedEvent.class)
    public void onClosed() throws InterruptedException {
        shutdown();
    }

    private void shutdown() throws InterruptedException {
        if (server != null) {
            log.info("Shutting down gRPC server...");
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}


