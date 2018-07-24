package de.hpi.swa.graal.squeak.test;

public final class SqueakSUnitTestMap {

    public static final Object[] SQUEAK_TEST_CASES = new Object[]{"AddPrefixNamePolicyTest", TEST_TYPE.PASSING,
                    "AliasTest", TEST_TYPE.PASSING,
                    "AllNamePolicyTest", TEST_TYPE.PASSING,
                    "AllocationTest", TEST_TYPE.IGNORE,
                    "ArbitraryObjectSocketTestCase", TEST_TYPE.FAILING,
                    "ArrayLiteralTest", TEST_TYPE.PASSING,
                    "ArrayTest", TEST_TYPE.PASSING,
                    "Ascii85ConverterTest", TEST_TYPE.PASSING,
                    "AssociationTest", TEST_TYPE.PASSING,
                    "BagTest", TEST_TYPE.PASSING,
                    "BalloonFontTest", TEST_TYPE.PASSING,
                    "Base64MimeConverterTest", TEST_TYPE.PASSING,
                    "BasicBehaviorClassMetaclassTest", TEST_TYPE.PASSING,
                    "BasicTypeTest", TEST_TYPE.PASSING,
                    "BecomeTest", TEST_TYPE.FAILING,
                    "BehaviorTest", TEST_TYPE.FAILING,
                    "BindingPolicyTest", TEST_TYPE.PASSING,
                    "BitBltClipBugs", TEST_TYPE.PASSING,
                    "BitBltSimulationTest", TEST_TYPE.PASSING,
                    "BitBltTest", TEST_TYPE.IGNORE, // passing, but very slow
                    "BitmapBugz", TEST_TYPE.PASSING,
                    "BitmapStreamTests", TEST_TYPE.IGNORE, // OOM error
                    "BitSetTest", TEST_TYPE.PASSING,
                    "BlockClosureTest", TEST_TYPE.IGNORE, // testRunSimulated does not work
                                                          // headless, testSourceString requires
                                                          // sources
                    "BlockLocalTemporariesRemovalTest", TEST_TYPE.PASSING,
                    "BMPReadWriterTest", TEST_TYPE.FAILING,
                    "BooleanTest", TEST_TYPE.PASSING,
                    "BrowserHierarchicalListTest", TEST_TYPE.PASSING,
                    "BrowserTest", TEST_TYPE.NOT_TERMINATING,
                    "BrowseTest", TEST_TYPE.PASSING,
                    "ByteArrayTest", TEST_TYPE.PASSING,
                    "BytecodeDecodingTests", TEST_TYPE.NOT_TERMINATING,
                    "ByteEncoderTest", TEST_TYPE.PASSING,
                    "CategorizerTest", TEST_TYPE.PASSING,
                    "ChainedSortFunctionTest", TEST_TYPE.PASSING,
                    "ChangeHooksTest", TEST_TYPE.NOT_TERMINATING,
                    "ChangeSetClassChangesTest", TEST_TYPE.NOT_TERMINATING,
                    "CharacterScannerTest", TEST_TYPE.PASSING,
                    "CharacterSetComplementTest", TEST_TYPE.PASSING,
                    "CharacterSetTest", TEST_TYPE.PASSING,
                    "CharacterTest", TEST_TYPE.PASSING,
                    "CircleMorphBugs", TEST_TYPE.PASSING,
                    "CircleMorphTest", TEST_TYPE.FLAKY,
                    "ClassAPIHelpBuilderTest", TEST_TYPE.PASSING,
                    "ClassBindingTest", TEST_TYPE.PASSING,
                    "ClassBuilderTest", TEST_TYPE.NOT_TERMINATING,
                    "ClassDescriptionTest", TEST_TYPE.PASSING,
                    "ClassFactoryForTestCaseTest", TEST_TYPE.IGNORE,
                    "ClassRemovalTest", TEST_TYPE.FLAKY,
                    "ClassRenameFixTest", TEST_TYPE.FLAKY,
                    "ClassTest", TEST_TYPE.IGNORE, // fails, and is very slow
                    "ClassTraitTest", TEST_TYPE.PASSING,
                    "ClassVarScopeTest", TEST_TYPE.IGNORE, // passes, but is very slow
                    "ClipboardTest", TEST_TYPE.PASSING,
                    "ClosureCompilerTest", TEST_TYPE.NOT_TERMINATING, // requires sources
                    "ClosureTests", TEST_TYPE.PASSING,
                    "CogVMBaseImageTests", TEST_TYPE.PASSING,
                    "CollectionTest", TEST_TYPE.PASSING,
                    "ColorTest", TEST_TYPE.PASSING,
                    "CompiledMethodComparisonTest", TEST_TYPE.NOT_TERMINATING,
                    "CompiledMethodTest", TEST_TYPE.PASSING,
                    "CompiledMethodTrailerTest", TEST_TYPE.PASSING,
                    "CompilerExceptionsTest", TEST_TYPE.PASSING,
                    "CompilerNotifyingTest", TEST_TYPE.FAILING,
                    "CompilerSyntaxErrorNotifyingTest", TEST_TYPE.FAILING,
                    "CompilerTest", TEST_TYPE.PASSING,
                    "ComplexTest", TEST_TYPE.PASSING,
                    "ContextCompilationTest", TEST_TYPE.PASSING,
                    "DataStreamTest", TEST_TYPE.PASSING,
                    "DateAndTimeEpochTest", TEST_TYPE.PASSING,
                    "DateAndTimeLeapTest", TEST_TYPE.BROKEN_IN_SQUEAK,
                    "DateAndTimeTest", TEST_TYPE.PASSING,
                    "DateTest", TEST_TYPE.PASSING,
                    "DebuggerExtensionsTest", TEST_TYPE.PASSING,
                    "DebuggerUnwindBug", TEST_TYPE.PASSING,
                    "DecompilerTests", TEST_TYPE.IGNORE, // slow
                    "DelayTest", TEST_TYPE.PASSING,
                    "DependencyBrowserTest", TEST_TYPE.IGNORE,
                    "DependentsArrayTest", TEST_TYPE.PASSING,
                    "DictionaryTest", TEST_TYPE.PASSING,
                    "DosFileDirectoryTests", TEST_TYPE.PASSING,
                    "DoubleByteArrayTest", TEST_TYPE.PASSING, // one failure in Squeak
                                                              // (BROKEN_IN_SQUEAK)
                    "DoubleWordArrayTest", TEST_TYPE.FLAKY, // two errors in Squeak
                                                            // (BROKEN_IN_SQUEAK)
                    "DurationTest", TEST_TYPE.PASSING,
                    "EnvironmentTest", TEST_TYPE.PASSING,
                    "EPSCanvasTest", TEST_TYPE.PASSING,
                    "EtoysStringExtensionTest", TEST_TYPE.PASSING,
                    "EventManagerTest", TEST_TYPE.PASSING,
                    "ExceptionTests", TEST_TYPE.FAILING,
                    "ExpandedSourceFileArrayTest", TEST_TYPE.PASSING,
                    "ExplicitNamePolicyTest", TEST_TYPE.PASSING,
                    "ExtendedNumberParserTest", TEST_TYPE.PASSING,
                    "FalseTest", TEST_TYPE.PASSING,
                    "FileContentsBrowserTest", TEST_TYPE.NOT_TERMINATING,
                    "FileDirectoryTest", TEST_TYPE.PASSING,
                    "FileList2ModalDialogsTest", TEST_TYPE.PASSING,
                    "FileListTest", TEST_TYPE.IGNORE,
                    "FileStreamTest", TEST_TYPE.PASSING,
                    "FileUrlTest", TEST_TYPE.PASSING,
                    "FlapTabTests", TEST_TYPE.PASSING,
                    "FloatArrayTest", TEST_TYPE.PASSING,
                    "FloatCollectionTest", TEST_TYPE.PASSING,
                    "FloatTest", TEST_TYPE.FAILING,
                    "FontTest", TEST_TYPE.PASSING,
                    "FormCanvasTest", TEST_TYPE.PASSING,
                    "FormTest", TEST_TYPE.PASSING,
                    "FractionTest", TEST_TYPE.PASSING,
                    "GeneratorTest", TEST_TYPE.PASSING,
                    "GenericUrlTest", TEST_TYPE.PASSING,
                    "GlobalTest", TEST_TYPE.PASSING,
                    "GradientFillStyleTest", TEST_TYPE.PASSING,
                    "HandBugs", TEST_TYPE.PASSING,
                    "HashAndEqualsTestCase", TEST_TYPE.PASSING,
                    "HashedCollectionTest", TEST_TYPE.PASSING,
                    "HashTesterTest", TEST_TYPE.PASSING,
                    "HeapTest", TEST_TYPE.PASSING,
                    "HelpBrowserTest", TEST_TYPE.IGNORE, // very slow
                    "HelpIconsTest", TEST_TYPE.PASSING,
                    "HelpTopicListItemWrapperTest", TEST_TYPE.PASSING,
                    "HelpTopicTest", TEST_TYPE.PASSING,
                    "HexTest", TEST_TYPE.PASSING,
                    "HierarchicalUrlTest", TEST_TYPE.PASSING,
                    "HierarchyBrowserTest", TEST_TYPE.PASSING,
                    "HtmlReadWriterTest", TEST_TYPE.PASSING,
                    "HttpUrlTest", TEST_TYPE.PASSING,
                    "IdentityBagTest", TEST_TYPE.PASSING,
                    "InstallerTest", TEST_TYPE.NOT_TERMINATING,
                    "InstallerUrlTest", TEST_TYPE.PASSING,
                    "InstructionClientTest", TEST_TYPE.PASSING,
                    "InstructionPrinterTest", TEST_TYPE.PASSING,
                    "InstVarRefLocatorTest", TEST_TYPE.PASSING,
                    "IntegerArrayTest", TEST_TYPE.PASSING,
                    "IntegerDigitLogicTest", TEST_TYPE.PASSING,
                    "IntegerTest", TEST_TYPE.PASSING,
                    "IntervalTest", TEST_TYPE.PASSING,
                    "IslandVMTweaksTestCase", TEST_TYPE.FAILING,
                    "JPEGReadWriter2Test", TEST_TYPE.FAILING,
                    "KeyedSetTest", TEST_TYPE.PASSING,
                    "LangEnvBugs", TEST_TYPE.FAILING,
                    "LargeNegativeIntegerTest", TEST_TYPE.PASSING,
                    "LargePositiveIntegerTest", TEST_TYPE.PASSING,
                    "LayoutFrameTest", TEST_TYPE.PASSING,
                    "LinkedListTest", TEST_TYPE.PASSING,
                    "LocaleTest", TEST_TYPE.FAILING,
                    "LongTestCaseTest", TEST_TYPE.PASSING,
                    "LongTestCaseTestUnderTest", TEST_TYPE.PASSING,
                    "MacFileDirectoryTest", TEST_TYPE.PASSING,
                    "MailAddressParserTest", TEST_TYPE.PASSING,
                    "MailDateAndTimeTest", TEST_TYPE.PASSING,
                    "MailMessageTest", TEST_TYPE.PASSING,
                    "MatrixTest", TEST_TYPE.PASSING,
                    "MCAncestryTest", TEST_TYPE.NOT_TERMINATING,
                    "MCChangeNotificationTest", TEST_TYPE.NOT_TERMINATING,
                    "MCClassDefinitionTest", TEST_TYPE.NOT_TERMINATING,
                    "MCDependencySorterTest", TEST_TYPE.PASSING,
                    "MCDictionaryRepositoryTest", TEST_TYPE.NOT_TERMINATING,
                    "MCDirectoryRepositoryTest", TEST_TYPE.NOT_TERMINATING,
                    "MCEnvironmentLoadTest", TEST_TYPE.NOT_TERMINATING,
                    "MCFileInTest", TEST_TYPE.NOT_TERMINATING,
                    "MCInitializationTest", TEST_TYPE.NOT_TERMINATING,
                    "MCMcmUpdaterTest", TEST_TYPE.PASSING,
                    "MCMczInstallerTest", TEST_TYPE.NOT_TERMINATING,
                    "MCMergingTest", TEST_TYPE.IGNORE,
                    "MCMethodDefinitionTest", TEST_TYPE.IGNORE,
                    "MCOrganizationTest", TEST_TYPE.IGNORE,
                    "MCPackageTest", TEST_TYPE.IGNORE,
                    "MCPatchTest", TEST_TYPE.IGNORE,
                    "MCPTest", TEST_TYPE.PASSING,
                    "MCScannerTest", TEST_TYPE.IGNORE,
                    "MCSerializationTest", TEST_TYPE.IGNORE,
                    "MCSnapshotBrowserTest", TEST_TYPE.IGNORE,
                    "MCSnapshotTest", TEST_TYPE.IGNORE,
                    "MCSortingTest", TEST_TYPE.PASSING,
                    "MCStReaderTest", TEST_TYPE.IGNORE,
                    "MCStWriterTest", TEST_TYPE.IGNORE,
                    "MCVersionNameTest", TEST_TYPE.IGNORE,
                    "MCVersionTest", TEST_TYPE.IGNORE,
                    "MCWorkingCopyRenameTest", TEST_TYPE.IGNORE,
                    "MCWorkingCopyTest", TEST_TYPE.IGNORE,
                    "MessageNamesTest", TEST_TYPE.PASSING,
                    "MessageSendTest", TEST_TYPE.PASSING,
                    "MessageSetTest", TEST_TYPE.PASSING,
                    "MessageTraceTest", TEST_TYPE.PASSING,
                    "MethodContextTest", TEST_TYPE.PASSING,
                    "MethodHighlightingTests", TEST_TYPE.FLAKY,
                    "MethodPragmaTest", TEST_TYPE.PASSING,
                    "MethodPropertiesTest", TEST_TYPE.PASSING,
                    "MethodReferenceTest", TEST_TYPE.PASSING,
                    "MIMEDocumentTest", TEST_TYPE.PASSING,
                    "MirrorPrimitiveTests", TEST_TYPE.FAILING,
                    "MiscPrimitivePluginTest", TEST_TYPE.FAILING, // failing in Squeak
                    "MonitorTest", TEST_TYPE.PASSING,
                    "MonthTest", TEST_TYPE.PASSING,
                    "MorphBugs", TEST_TYPE.PASSING,
                    "MorphicEventDispatcherTests", TEST_TYPE.PASSING,
                    "MorphicEventFilterTests", TEST_TYPE.PASSING,
                    "MorphicEventTests", TEST_TYPE.NOT_TERMINATING,
                    "MorphicExtrasSymbolExtensionsTest", TEST_TYPE.PASSING,
                    "MorphicToolBuilderTests", TEST_TYPE.PASSING,
                    "MorphicUIManagerTest", TEST_TYPE.FAILING,
                    "MorphTest", TEST_TYPE.PASSING,
                    "MultiByteFileStreamTest", TEST_TYPE.IGNORE,
                    "MVCToolBuilderTests", TEST_TYPE.NOT_TERMINATING,
                    "NamePolicyTest", TEST_TYPE.PASSING,
                    "NumberParsingTest", TEST_TYPE.PASSING,
                    "NumberTest", TEST_TYPE.PASSING,
                    "ObjectFinalizerTests", TEST_TYPE.PASSING,
                    "ObjectTest", TEST_TYPE.PASSING,
                    "OrderedCollectionInspectorTest", TEST_TYPE.PASSING,
                    "OrderedCollectionTest", TEST_TYPE.PASSING,
                    "OrderedDictionaryTest", TEST_TYPE.PASSING,
                    "PackageDependencyTest", TEST_TYPE.NOT_TERMINATING,
                    "PackagePaneBrowserTest", TEST_TYPE.PASSING,
                    "ParserEditingTest", TEST_TYPE.PASSING,
                    "PasteUpMorphTest", TEST_TYPE.PASSING,
                    "PCCByCompilationTest", TEST_TYPE.IGNORE,
                    "PCCByLiteralsTest", TEST_TYPE.IGNORE,
                    "PluggableMenuItemSpecTests", TEST_TYPE.PASSING,
                    "PluggableTextMorphTest", TEST_TYPE.PASSING,
                    "PNGReadWriterTest", TEST_TYPE.NOT_TERMINATING,
                    "PointTest", TEST_TYPE.PASSING,
                    "PolygonMorphTest", TEST_TYPE.PASSING,
                    "PreferencesTest", TEST_TYPE.PASSING,
                    "ProcessSpecificTest", TEST_TYPE.PASSING,
                    "ProcessTerminateBug", TEST_TYPE.FLAKY,
                    "ProcessTest", TEST_TYPE.PASSING,
                    "PromiseTest", TEST_TYPE.PASSING,
                    "ProtoObjectTest", TEST_TYPE.PASSING,
                    "PureBehaviorTest", TEST_TYPE.NOT_TERMINATING,
                    "RandomTest", TEST_TYPE.IGNORE, // passing, but very slow
                    "ReadStreamTest", TEST_TYPE.PASSING,
                    "ReadWriteStreamTest", TEST_TYPE.PASSING,
                    "RecentMessagesTest", TEST_TYPE.PASSING,
                    "RectangleTest", TEST_TYPE.PASSING,
                    "ReferenceStreamTest", TEST_TYPE.PASSING,
                    "ReleaseTest", TEST_TYPE.NOT_TERMINATING,
                    "RemoteStringTest", TEST_TYPE.PASSING,
                    "RemovePrefixNamePolicyTest", TEST_TYPE.PASSING,
                    "RenderBugz", TEST_TYPE.PASSING,
                    "ResumableTestFailureTestCase", TEST_TYPE.PASSING,
                    "RunArrayTest", TEST_TYPE.PASSING,
                    "RWBinaryOrTextStreamTest", TEST_TYPE.FAILING,
                    "RxMatcherTest", TEST_TYPE.PASSING,
                    "RxParserTest", TEST_TYPE.PASSING,
                    "ScaledDecimalTest", TEST_TYPE.PASSING,
                    "ScannerTest", TEST_TYPE.PASSING,
                    "ScheduleTest", TEST_TYPE.PASSING,
                    "ScrollBarTest", TEST_TYPE.PASSING,
                    "ScrollPaneLeftBarTest", TEST_TYPE.PASSING,
                    "ScrollPaneRetractableBarsTest", TEST_TYPE.PASSING,
                    "ScrollPaneTest", TEST_TYPE.PASSING,
                    "SecureHashAlgorithmTest", TEST_TYPE.PASSING,
                    "SemaphoreTest", TEST_TYPE.FAILING, // testSemaInCriticalWait failing in Squeak
                    "SequenceableCollectionTest", TEST_TYPE.PASSING,
                    "SetTest", TEST_TYPE.PASSING,
                    "SetWithNilTest", TEST_TYPE.PASSING,
                    "SharedQueue2Test", TEST_TYPE.IGNORE, // passing, sometimes blocks on CI
                    "SHParserST80Test", TEST_TYPE.PASSING,
                    "SimpleSwitchMorphTest", TEST_TYPE.PASSING,
                    "SimpleTestResourceTestCase", TEST_TYPE.PASSING,
                    "SliderTest", TEST_TYPE.PASSING,
                    "SmallIntegerTest", TEST_TYPE.PASSING,
                    "SmalltalkImageTest", TEST_TYPE.PASSING,
                    "SmartRefStreamTest", TEST_TYPE.IGNORE, // flaky and slow
                    "SMDependencyTest", TEST_TYPE.PASSING,
                    "SMTPClientTest", TEST_TYPE.IGNORE,
                    "SocketStreamTest", TEST_TYPE.PASSING,
                    "SocketTest", TEST_TYPE.FAILING, // testSocketReuse and testSendTimeout fail
                    "SortedCollectionTest", TEST_TYPE.PASSING,
                    "SortFunctionTest", TEST_TYPE.PASSING,
                    "SqNumberParserTest", TEST_TYPE.PASSING,
                    "SqueakSSLTest", TEST_TYPE.FAILING,
                    "ST80MenusTest", TEST_TYPE.PASSING,
                    "ST80PackageDependencyTest", TEST_TYPE.PASSING,
                    "StackTest", TEST_TYPE.PASSING,
                    "StandardSourceFileArrayTest", TEST_TYPE.PASSING,
                    "StickynessBugz", TEST_TYPE.PASSING,
                    "StopwatchTest", TEST_TYPE.PASSING,
                    "StringSocketTestCase", TEST_TYPE.FAILING,
                    "StringTest", TEST_TYPE.FAILING,
                    "SumBugs", TEST_TYPE.PASSING,
                    "SUnitExtensionsTest", TEST_TYPE.PASSING,
                    "SUnitTest", TEST_TYPE.NOT_TERMINATING,
                    "SUnitToolBuilderTests", TEST_TYPE.IGNORE, // passing, but very slow
                    "SymbolTest", TEST_TYPE.PASSING,
                    "SystemChangeErrorHandling", TEST_TYPE.IGNORE, // used to pass
                    "SystemChangeFileTest", TEST_TYPE.IGNORE,
                    "SystemChangeNotifierTest", TEST_TYPE.PASSING,
                    "SystemChangeTestRoot", TEST_TYPE.PASSING,
                    "SystemDictionaryTest", TEST_TYPE.PASSING,
                    "SystemNavigationTest", TEST_TYPE.PASSING,
                    "SystemOrganizerTest", TEST_TYPE.PASSING,
                    "SystemVersionTest", TEST_TYPE.PASSING,
                    "TestIndenting", TEST_TYPE.PASSING,
                    "TestNewParagraphFix", TEST_TYPE.PASSING,
                    "TestObjectsAsMethods", TEST_TYPE.PASSING,
                    "TestParagraphFix", TEST_TYPE.PASSING,
                    "TestSpaceshipOperator", TEST_TYPE.PASSING,
                    "TestURI", TEST_TYPE.PASSING,
                    "TestValueWithinFix", TEST_TYPE.NOT_TERMINATING,
                    "TestVMStatistics", TEST_TYPE.PASSING,
                    "TextAlignmentTest", TEST_TYPE.PASSING,
                    "TextAnchorTest", TEST_TYPE.PASSING,
                    "TextAndTextStreamTest", TEST_TYPE.PASSING,
                    "TextAttributesScanningTest", TEST_TYPE.IGNORE, // FIXME: NonBooleanReceiver
                    "TextDiffBuilderTest", TEST_TYPE.PASSING,
                    "TextEditorTest", TEST_TYPE.PASSING,
                    "TextEmphasisTest", TEST_TYPE.PASSING,
                    "TextFontChangeTest", TEST_TYPE.PASSING,
                    "TextFontReferenceTest", TEST_TYPE.PASSING,
                    "TextKernTest", TEST_TYPE.PASSING,
                    "TextLineEndingsTest", TEST_TYPE.PASSING,
                    "TextLineTest", TEST_TYPE.PASSING,
                    "TextMorphTest", TEST_TYPE.PASSING,
                    "TextStyleTest", TEST_TYPE.PASSING,
                    "TextTest", TEST_TYPE.PASSING,
                    "ThirtyTwoBitRegisterTest", TEST_TYPE.NOT_TERMINATING,
                    "TileMorphTest", TEST_TYPE.PASSING,
                    "TimespanDoSpanAYearTest", TEST_TYPE.PASSING,
                    "TimespanDoTest", TEST_TYPE.PASSING,
                    "TimespanTest", TEST_TYPE.PASSING,
                    "TimeStampTest", TEST_TYPE.PASSING,
                    "TimeTest", TEST_TYPE.PASSING,
                    "TraitCompositionTest", TEST_TYPE.NOT_TERMINATING,
                    "TraitFileOutTest", TEST_TYPE.NOT_TERMINATING,
                    "TraitMethodDescriptionTest", TEST_TYPE.NOT_TERMINATING,
                    "TraitsTestCase", TEST_TYPE.PASSING,
                    "TraitSystemTest", TEST_TYPE.IGNORE, // passing, very slow
                    "TraitTest", TEST_TYPE.IGNORE, // passing, but very slow
                    "TrueTest", TEST_TYPE.PASSING,
                    "UndefinedObjectTest", TEST_TYPE.PASSING,
                    "UnderscoreSelectorsTest", TEST_TYPE.PASSING,
                    "UnimplementedCallBugz", TEST_TYPE.PASSING,
                    "UnixFileDirectoryTests", TEST_TYPE.PASSING,
                    "UrlTest", TEST_TYPE.PASSING,
                    "UserInterfaceThemeTest", TEST_TYPE.NOT_TERMINATING,
                    "UTF16TextConverterTest", TEST_TYPE.BROKEN_IN_SQUEAK,
                    "UTF32TextConverterTest", TEST_TYPE.PASSING,
                    "UTF8TextConverterTest", TEST_TYPE.FAILING, // FIXME: NullPointerException
                    "UTF8EdgeCaseTest", TEST_TYPE.FAILING, // failing in Squeak
                    "UUIDPrimitivesTest", TEST_TYPE.PASSING,
                    "UUIDTest", TEST_TYPE.PASSING,
                    "VersionNumberTest", TEST_TYPE.PASSING,
                    "WeakFinalizersTest", TEST_TYPE.PASSING,
                    "WeakIdentityKeyDictionaryTest", TEST_TYPE.FLAKY,
                    "WeakMessageSendTest", TEST_TYPE.FAILING, // failing since incr. GC != full GC
                    "WeakRegistryTest", TEST_TYPE.NOT_TERMINATING, // flaky and slow (uses Delays)
                    "WeakSetInspectorTest", TEST_TYPE.IGNORE, // doesn't terminate sometimes on JDK8
                    "WeakSetTest", TEST_TYPE.FLAKY,
                    "WebClientServerTest", TEST_TYPE.FAILING,
                    "WeekTest", TEST_TYPE.PASSING,
                    "WideCharacterSetTest", TEST_TYPE.PASSING,
                    "WideStringTest", TEST_TYPE.NOT_TERMINATING,
                    "Win32VMTest", TEST_TYPE.PASSING,
                    "WordArrayTest", TEST_TYPE.PASSING,
                    "WorldStateTest", TEST_TYPE.NOT_TERMINATING,
                    "WriteStreamTest", TEST_TYPE.PASSING,
                    "XMLParserTest", TEST_TYPE.PASSING,
                    "YearMonthWeekTest", TEST_TYPE.PASSING,
                    "YearTest", TEST_TYPE.PASSING};

    public static final class TEST_TYPE {
        public static final String PASSING = "Passing"; // should pass
        public static final String FAILING = "Failing"; // some/all test selectors fail/error
        public static final String NOT_TERMINATING = "Not Terminating"; // does not terminate
        public static final String BROKEN_IN_SQUEAK = "Broken in Squeak"; // not working in Squeak
        public static final String FLAKY = "Flaky"; // flaky tests
        public static final String IGNORE = "Ignored"; // unable to run (e.g. OOM, ...)
    }

}
