package com.picturetojson.repository;

import com.picturetojson.entity.FieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldMappingRepository extends JpaRepository<FieldMapping, Long> {
    
    List<FieldMapping> findByDocumentId(Long documentId);
    
    List<FieldMapping> findBySourceField(String sourceField);
    
    List<FieldMapping> findByTargetField(String targetField);
}