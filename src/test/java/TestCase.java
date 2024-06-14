/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.catacombae.hfsexplorer.types.alias.AliasHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-11-21 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
@EnabledIf("localPropertiesExists")
public class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "hfs")
    String hfs = "src/test/resources/test.hfs";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test1() throws Exception {
        Path path = Paths.get(hfs);
        byte[] bytes = Files.readAllBytes(path);
        AliasHeader header = new AliasHeader(bytes, 0);
        header.printFields(System.out, "");
    }
}
