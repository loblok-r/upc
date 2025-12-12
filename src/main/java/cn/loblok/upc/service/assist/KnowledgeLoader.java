package cn.loblok.upc.service.assist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class KnowledgeLoader {

    @Value("${knowledge.file-path}")
    private Resource knowledgeFile;

    private JsonNode knowledgeJson;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadKnowledge() throws IOException {
        try (InputStream is = knowledgeFile.getInputStream()) {
            this.knowledgeJson = objectMapper.readTree(is);
        }
    }

    public JsonNode getKnowledgeJson() {
        return knowledgeJson;
    }
}