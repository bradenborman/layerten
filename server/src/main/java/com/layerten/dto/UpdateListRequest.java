package com.layerten.dto;

import java.util.Set;

/**
 * Request DTO for updating an existing ranked list.
 */
public record UpdateListRequest(
    String title,
    String subtitle,
    String intro,
    String outro,
    Long coverImageId,
    Set<Long> tagIds
) {}
