package com.src.main.http;

import java.util.Optional;

import com.src.main.dto.MavenDependencyDTO;

public interface RemoteDependencyLookup {
    Optional<MavenDependencyDTO> findByKeyword(String keyword);
}
