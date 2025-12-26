package com.revet.documents.security

/**
 * Types of resources that can be secured with permission checks.
 * Used with @SecuredBy annotation.
 */
enum class ResourceType {
    DOCUMENT,
    PROJECT,
    ORGANIZATION
}
