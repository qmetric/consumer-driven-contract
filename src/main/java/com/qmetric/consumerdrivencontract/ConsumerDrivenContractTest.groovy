package com.qmetric.consumerdrivencontract

import java.lang.annotation.*

@Retention(RetentionPolicy.RUNTIME) @Target([ElementType.TYPE, ElementType.METHOD]) @Inherited
public @interface ConsumerDrivenContractTest {}