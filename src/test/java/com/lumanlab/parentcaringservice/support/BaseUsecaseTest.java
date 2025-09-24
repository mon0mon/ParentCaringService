package com.lumanlab.parentcaringservice.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class BaseUsecaseTest {

}
