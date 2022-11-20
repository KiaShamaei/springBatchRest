package com.example.demo.logger;

import com.example.demo.domain.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class LoggingItemWriter implements ItemWriter<Users> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingItemWriter.class);

    @Override
    public void write(List<? extends Users> list) throws Exception {
        LOGGER.info(">>>> Writing students: {}", list);
    }
}
