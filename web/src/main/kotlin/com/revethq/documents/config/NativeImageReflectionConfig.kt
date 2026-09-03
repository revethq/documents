package com.revethq.documents.config

import com.revethq.core.Identifier
import com.revethq.core.Metadata
import com.revethq.core.SchemaValidation
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Registers classes for reflection in GraalVM native image.
 *
 * [KotlinJsonFormatMapper] is loaded by Hibernate via `Class.forName()`, and
 * [Metadata]/[Identifier]/[SchemaValidation] are deserialized from JSON columns.
 */
@RegisterForReflection(
    targets = [
        KotlinJsonFormatMapper::class,
        Metadata::class,
        Identifier::class,
        SchemaValidation::class,
    ],
)
class NativeImageReflectionConfig
