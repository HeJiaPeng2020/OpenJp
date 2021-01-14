package open.jp.annotation;

import java.lang.annotation.Annotation;

@AnnotationOne(one="testOne")
@AnnotationTwo(two="testTwo")
public class TestAnnotation
{
    public static void main(String[] args)
    {
        System.out.println(111);
        System.out.println(222);
        Class<TestAnnotation> testAnnotationClass = TestAnnotation.class;

        Annotation[] annotations = testAnnotationClass.getAnnotations();

        for(Annotation annotation: annotations)
        {   System.out.println(annotation.getClass().getName());
        }
    }
}
