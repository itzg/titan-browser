package me.itzg.titanbrowser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * <p>Copyright &copy; 2015 Geoff Bourne. All rights reserved.</p>
 *
 * @author itzg
 * @since 12/4/2014
 */
@Configuration
@ComponentScan(scopedProxy = ScopedProxyMode.INTERFACES)
@EnableAutoConfiguration
public class TitanBrowserApp {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(TitanBrowserApp.class, args);
    }
}
