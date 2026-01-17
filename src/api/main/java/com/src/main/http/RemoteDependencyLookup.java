package com.src.main.http;

import java.util.Optional;

import com.src.main.dto.MavenDependency;

public interface RemoteDependencyLookup {
    Optional<MavenDependency> findByKeyword(String keyword);
}
