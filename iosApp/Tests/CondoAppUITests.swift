import XCTest

final class CondoAppUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        XCUIDevice.shared.orientation = .portrait
        app = XCUIApplication()
        app.launch()
    }

    override func tearDownWithError() throws {
        app.terminate()
    }

    // Run against a fresh simulator in CONDO_ENVIRONMENT=demo.
    // Queries use the labels exposed by Compose to native accessibility.
    func testDemoLoginParcelsAndCondominiumSwitch() {
        waitForLabel("Bem-vindo de volta")
