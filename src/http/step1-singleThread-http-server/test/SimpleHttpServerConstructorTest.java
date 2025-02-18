import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SimpleHttpServerConstructorTest {

    @Test
    @DisplayName("port:-8080")
    void constructorPortCheck(){
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            new SimpleHttpServer(-8080);
        });
    }
    
}