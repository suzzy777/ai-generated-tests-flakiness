I need you to fix an error in a unit test, an error occurred while compiling and executing

The unit test is:
```
${unit_test}
```

The error chatMessage is:
```
${error_message}
```

The unit test is testing the method `${method_sig}` in the class `${class_name}`,
the source code of the method under test and its class is:
```
${full_fm}
```
<#if other_method_sigs?has_content>
```
The signatures of other methods in its class are `${other_method_sigs}`
```
</#if>

Please fix the error and return the whole fixed unit test. You can use Junit 5, Mockito 3 and reflection. If the test changes a static field, a singleton or a field set by reflection, save the original value and restore it in an @AfterEach method. No explanation is needed.