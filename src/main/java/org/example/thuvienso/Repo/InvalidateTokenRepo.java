package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.InvalidateTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidateTokenRepo extends JpaRepository<InvalidateTokenEntity,String> {
}
