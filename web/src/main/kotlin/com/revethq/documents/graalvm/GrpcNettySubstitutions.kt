package com.revethq.documents.graalvm

import com.oracle.svm.core.annotate.Substitute
import com.oracle.svm.core.annotate.TargetClass

/**
 * GraalVM native image substitutions for grpc-netty-shaded's Netty logger factories.
 *
 * The grpc-netty-shaded jar includes Netty's Log4J and Log4J2 logger factories that
 * reference Log4J classes not on the classpath. These substitutions replace the factory
 * methods so GraalVM doesn't trace into the missing Log4J dependencies.
 *
 * At runtime, Netty's InternalLoggerFactory detects that Log4J is not available and
 * falls back to JDK/SLF4J logging, so these factories are never actually used.
 */
@TargetClass(className = "io.grpc.netty.shaded.io.netty.util.internal.logging.InternalLogger")
class GrpcInternalLogger

@TargetClass(className = "io.grpc.netty.shaded.io.netty.util.internal.logging.Log4J2LoggerFactory")
class GrpcLog4J2LoggerFactorySubstitution {
    @Substitute
    protected fun newInstance(name: String): GrpcInternalLogger = throw UnsupportedOperationException("Log4J2 is not available")
}

@TargetClass(className = "io.grpc.netty.shaded.io.netty.util.internal.logging.Log4JLoggerFactory")
class GrpcLog4JLoggerFactorySubstitution {
    @Substitute
    protected fun newInstance(name: String): GrpcInternalLogger = throw UnsupportedOperationException("Log4J is not available")
}
