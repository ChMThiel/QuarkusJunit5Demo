package org.acme;

import static org.acme.TestSuite.MY_TAG;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @since 19.10.2021
 */
class PojoTest {

    @Test
    @Tag(MY_TAG)
    @DisplayName("shouldCheckSomething")
    public void shouldCheckSomething() {
    }

}
