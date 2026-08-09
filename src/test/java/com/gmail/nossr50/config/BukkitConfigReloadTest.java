package com.gmail.nossr50.config;

import static com.gmail.nossr50.datatypes.skills.PrimarySkillType.MINING;
import static java.util.logging.Logger.getLogger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.mcMMO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BukkitConfigReloadTest {
    private static final Logger logger = getLogger(BukkitConfigReloadTest.class.getName());

    private MockedStatic<mcMMO> mockedMcMMO;
    private File testDataFolder;

    @BeforeEach
    void setUp() throws IOException {
        mockedMcMMO = mockStatic(mcMMO.class);
        mcMMO.p = mock(mcMMO.class);
        testDataFolder = Files.createTempDirectory("mcmmo-config-reload-").toFile();
        when(mcMMO.p.getDataFolder()).thenReturn(testDataFolder);
        when(mcMMO.p.getLogger()).thenReturn(logger);

        final Server server = mock(Server.class);
        final PluginManager pluginManager = mock(PluginManager.class);
        when(mcMMO.p.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);

        when(mcMMO.p.getResource(eq("experience.yml"))).thenAnswer(invocation ->
                BukkitConfigReloadTest.class.getClassLoader().getResourceAsStream("experience.yml"));
        doAnswer(invocation -> {
            copyBundledResource("experience.yml");
            return null;
        }).when(mcMMO.p).saveResource(eq("experience.yml"), anyBoolean());
    }

    @AfterEach
    void tearDown() throws ReflectiveOperationException, IOException {
        resetExperienceConfigInstance();
        mockedMcMMO.close();
        try (Stream<Path> paths = Files.walk(testDataFolder.toPath())) {
            paths.sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    @Test
    void experienceConfigReloadShouldRefreshBlockExperienceFromDisk() throws Exception {
        copyBundledResource("experience.yml");
        resetExperienceConfigInstance();

        final ExperienceConfig experienceConfig = ExperienceConfig.getInstance();
        assertThat(experienceConfig.getXp(MINING, Material.TUFF)).isEqualTo(10);

        final Path configPath = testDataFolder.toPath().resolve("experience.yml");
        final String yaml = Files.readString(configPath);
        Files.writeString(configPath, yaml.replaceFirst("(?m)^\\s*Tuff: 10\\s*$", "        Tuff: 77"));

        experienceConfig.reload();

        assertThat(experienceConfig.getXp(MINING, Material.TUFF)).isEqualTo(77);
    }

    private void copyBundledResource(final String resourceName) throws IOException {
        try (InputStream inputStream =
                BukkitConfigReloadTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            assertThat(inputStream).as("bundled " + resourceName).isNotNull();
            Files.copy(inputStream, testDataFolder.toPath().resolve(resourceName),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void resetExperienceConfigInstance() throws ReflectiveOperationException {
        final Field instanceField = ExperienceConfig.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}
