/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.plugin.google.sheets;

import com.google.common.collect.ImmutableMap;
import io.prestosql.spi.Plugin;
import io.prestosql.spi.connector.Connector;
import io.prestosql.spi.connector.ConnectorFactory;
import io.prestosql.testing.TestingConnectorContext;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Base64;
import java.util.Scanner;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.testng.Assert.assertNotNull;

public class TestSheetsPlugin
{
    static final String TEST_METADATA_SHEET_ID = "1Es4HhWALUQjoa-bQh4a8B5HROz7dpGMfq_HbfoaW5LM";

    static String getTestCredentialsPath() throws Exception
    {
        try (InputStream in =
                ClassLoader.getSystemResourceAsStream("gsheets-creds-base64-encoded.props")) {
            Scanner scn = new Scanner(in);
            StringBuilder credsBase64Encoded = new StringBuilder();

            while (scn.hasNextLine()) {
                credsBase64Encoded.append(scn.nextLine());
            }
            String credentials = new String(Base64.getDecoder().decode(credsBase64Encoded.toString()));

            File tempFile =
                    File.createTempFile(
                            System.getProperty("java.io.tmpdir"),
                            "credentials-" + System.currentTimeMillis() + ".json");
            try (FileWriter fw = new FileWriter(tempFile)) {
                fw.append(credentials);
                fw.flush();
            }
            tempFile.deleteOnExit();
            return tempFile.getAbsolutePath();
        }
    }

    @Test
    public void testCreateConnector() throws Exception
    {
        Plugin plugin = new SheetsPlugin();
        ConnectorFactory factory = getOnlyElement(plugin.getConnectorFactories());
        Connector c = factory.create(
                            TestGoogleSheets.GOOGLE_SHEETS,
                            ImmutableMap.of("credentials-path", getTestCredentialsPath(),
                        "metadata-sheet-id", TEST_METADATA_SHEET_ID),
                            new TestingConnectorContext());
        assertNotNull(c);
    }
}
