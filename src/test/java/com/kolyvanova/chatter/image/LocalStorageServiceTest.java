package com.kolyvanova.chatter.image;

import com.kolyvanova.chatter.exception.InvalidFileException;
import com.kolyvanova.chatter.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class LocalStorageServiceTest extends AbstractIntegrationTest {

    @Autowired
    private LocalStorageService localStorageService;

    @Test
    void loadAndDeleteRejectAPathOutsideTheMediaDirectory() {
        InvalidFileException load = assertThrows(InvalidFileException.class,
                () -> localStorageService.load("../outside.png"));
        InvalidFileException delete = assertThrows(InvalidFileException.class,
                () -> localStorageService.delete("../outside.png"));

        assertEquals("error.file.path", load.getMessage());
        assertEquals("error.file.path", delete.getMessage());
        assertDoesNotThrow(() -> localStorageService.delete(null));
    }
}
