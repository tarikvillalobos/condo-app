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
