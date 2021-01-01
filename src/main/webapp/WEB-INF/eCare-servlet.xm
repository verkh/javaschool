<beans xmlns = "http://www.springframework.org/schema/beans"
       xmlns:context = "http://www.springframework.org/schema/context"
       xmlns:xsi = "http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation = "http://www.springframework.org/schema/beans
   http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
   http://www.springframework.org/schema/context
   http://www.springframework.org/schema/context/spring-context-3.0.xsd
   http://www.springframework.org/schema/mvc
   http://www.springframework.org/schema/mvc/spring-mvc-4.0.xsd">

    <context:component-scan base-package = "com.ecare" />
    <mvc:annotation-driven/>

    <bean class = "org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name = "prefix" value = "/views/" />
        <property name = "suffix" value = ".jsp" />
    </bean>

    <mvc:resources mapping="/css/**" location="/css/" cache-period="31556926"/>
    <mvc:resources mapping="/images/**" location="/images/" cache-period="31556926"/>
    <mvc:resources mapping="/videos/**" location="/videos/" cache-period="31556926"/>

</beans>