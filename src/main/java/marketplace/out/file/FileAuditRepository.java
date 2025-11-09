package marketplace.out.file;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Репозиторий аудита: добавляет строки в файл audit.log
 */

public class FileAuditRepository {
    private final Path file;

    public FileAuditRepository(Path file){
        this.file = file;
    }

    public synchronized void append(String line){
        try(FileWriter fileWriter = new FileWriter(file.toFile(), true)){
            fileWriter.write(line);
            fileWriter.write(System.lineSeparator());
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
