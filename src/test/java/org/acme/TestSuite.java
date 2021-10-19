package org.acme;

import static org.acme.TestSuite.MY_TAG;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("org.acme")
@IncludeTags(MY_TAG)
public class TestSuite {

    public static final String MY_TAG = "myTag";
}
