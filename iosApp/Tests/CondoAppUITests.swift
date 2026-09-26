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
        capture("01-login")
        tapLabel("Entrar na demonstração")
        waitForLabel("Olá, Alex")
        waitForLabel("Residencial Jardim Aurora")
        capture("02-home")

        tapLabel("Encomendas")
        waitForLabel("Todas")
        tapLabel("Mercado Livre")
        waitForLabel("Rastreio DEMO-001")
        waitForLabel("Locker Portaria")
        capture("03-parcel-detail")
        tapLabel("Voltar")
        waitForLabel("Todas")

        tapLabel("Início")
        waitForLabel("Residencial Jardim Aurora")
        tapLabel("Residencial Jardim Aurora")
        waitForLabel("Escolha qual condomínio você quer ver agora")
        tapLabel("Vila das Águas")
        waitForLabel("Olá, Alex")
        waitForLabel("Vila das Águas")
        XCTAssertFalse(labelQuery("Residencial Jardim Aurora").firstMatch.exists)
        capture("04-second-condominium")

        tapLabel("Vila das Águas")
        waitForLabel("Escolha qual condomínio você quer ver agora")
        tapLabel("Residencial Jardim Aurora")
        waitForLabel("Olá, Alex")
        waitForLabel("Residencial Jardim Aurora")
    }

    private func labelQuery(_ label: String) -> XCUIElementQuery {
        // Clickable surfaces merge their title and subtitle on iOS.
        app.descendants(matching: .any).matching(
            NSPredicate(format: "label BEGINSWITH %@", label)
        )
    }

    private func waitForLabel(_ label: String, file: StaticString = #filePath, line: UInt = #line) {
    }

    private func tapLabel(_ label: String, file: StaticString = #filePath, line: UInt = #line) {
        for _ in 0..<8 {
            if let element = labelQuery(label).allElementsBoundByIndex.first(where: { $0.isHittable }) {
                element.tap()
                return
            }
            app.swipeUp()
        }
        XCTFail("Could not reach accessible control: \(label)", file: file, line: line)
    }

    private func capture(_ name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
