package com.urisik.backend.global.ai;

import java.util.Optional;

public interface AiImageClient {
    Optional<byte[]> generateImage(String prompt);
}

