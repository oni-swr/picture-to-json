package com.picturetojson.repository;

import com.picturetojson.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByStatus(Document.ProcessingStatus status);
    
    Page<Document> findByStatusOrderByCreatedAtDesc(Document.ProcessingStatus status, Pageable pageable);
    
    @Query("SELECT d FROM Document d WHERE d.status = :status AND d.processingProgress < 100")
    List<Document> findIncompleteDocumentsByStatus(@Param("status") Document.ProcessingStatus status);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.status = :status")
    long countByStatus(@Param("status") Document.ProcessingStatus status);
    
    List<Document> findByOriginalFilenameContainingIgnoreCase(String filename);
}