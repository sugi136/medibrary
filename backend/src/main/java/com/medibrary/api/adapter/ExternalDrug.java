package com.medibrary.api.adapter;

public record ExternalDrug(
        String id, String name, String shape, String color, String markFront, String markBack, String imageUrl
) { }
