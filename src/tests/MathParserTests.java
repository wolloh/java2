package tests;
import static org.junit.Assert.*;
import org.junit.Test;
import Mathparse.MathParse;
import java.util.NoSuchElementException;

public class MathParserTests {
    @Test
    public void failtoparse() {
        MathParse parser = new MathParse();
        String expr = "";
        assertThrows(NoSuchElementException.class,()->parser.Parse(expr));
    }
    @Test
    public void validparse(){
        MathParse parser=new MathParse();
        String expr="2*10";
        assertEquals(20.0,parser.Parse(expr),0.1);
    }
    @Test
    public void failtoparsenoclosingbracket(){
        MathParse parser=new MathParse();
        String expr="2*sin(x+3*a";
        assertThrows(NoSuchElementException.class,()->parser.Parse(expr));
    }
    @Test
    public void failtoparsenotvalidexpr(){
        MathParse parser=new MathParse();
        String expr="3@8328321)9!";
        assertThrows(RuntimeException.class,()->parser.Parse(expr));
    }
    @Test
    public void failtoparsewithnovalidenteredelem(){
        MathParse parser=new MathParse();
        String expr="sin(x)";
        assertThrows(NoSuchElementException.class,()->parser.Parse(expr));
    }
}
