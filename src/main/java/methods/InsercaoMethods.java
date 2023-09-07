package methods;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.restassured.path.json.JsonPath;
import org.openqa.selenium.By;
import org.testng.Assert;
import pageobject.*;
import utils.Utils;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.closeWindow;
import static configuration.SelenideConfiguration.configurationSelenide;
import static constants.GenericConstants.END_DATE;
import static constants.GenericConstants.START_DATE;

public class InsercaoMethods {

    public LoginPage login;
    public HomePage home;
    public SuprimentosLogisticaPage suprimentos;
    public PrescricaoDiretaPage prescricaoDireta;
    public ItensPage itens;



    public void configuracao() {
        configurationSelenide();
        clearBrowserCookies();
        login = new LoginPage();
        home = new HomePage();
        suprimentos = new SuprimentosLogisticaPage();
        prescricaoDireta = new PrescricaoDiretaPage();
        itens = new ItensPage();
    }

    // Recebe um jsonPath e verifica se é null se for retorna true
    private boolean jsonPathIsNull(JsonPath jsonPath) {
        return jsonPath.getString("items[0].custom_code") == null;
    }

    // Realiza uma consulta na API inserindo o ultimo id se o resultado for null ele para se nao ele cria o arquivo yaml

    protected void consultarDados() throws Exception {
        String ultimoId = Utils.lerArquivo("ultimoItemInserido", "last_id");
        JsonPath jsonPath = Utils.consultarMassaDeDados(START_DATE, END_DATE, ultimoId);

        try {
            if (!jsonPathIsNull(jsonPath)) {
                processarDadosConsulta(jsonPath, ultimoId);
            } else {
                Utils.criarArquivoUltimoID(ultimoId);
                Utils.criarArquivo(null, ultimoId, null);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // metodo para criacao dos arquivos yaml com os dados da consulta da API
    private void processarDadosConsulta(JsonPath jsonPath, String ultimoId) throws Exception {
        Utils.criarArquivo(
                jsonPath.getString("items[0].custom_code"),
                jsonPath.getString("items[0].id"),
                jsonPath.getString("items[0].quantity"));
        Utils.criarArquivoUltimoID(ultimoId);
    }


    // acessa o sistema e seleciona o paciente
    private void acessarESelecionarPaciente() throws Exception {
        open("https://guarulhoshospitalar.sissonline.com.br/Abertura/Login.aspx");
        login.logar();
        home.acessarModuloSuprimentos();
        suprimentos.acessarPrescricaoDireta();
        prescricaoDireta.selecionarPaciente("PACIENTE TESTE");
    }

    // pesquisa e seleciona o item consultando o arquivo yaml
    private void pesquisaItemEPreencheDadosDaInsercao(String lastCustomCode, String lastQuantity, String bkpId) throws Exception {
        itens.pesquisarItens("ALTEPLASE");
        itens.realizarNovaPesquisaItem(lastCustomCode);
        SelenideElement optLista = $(By.xpath("//*/tr/td[contains(text(),'" + lastCustomCode + "')]"));

        if (optLista.is(visible)) {
            optLista.click();
            itens.btnConfirmar.isEnabled();
            itens.btnConfirmar.click();
            itens.inserirItens(lastQuantity);
            itens.verificarMensagemSucesso();
        }else{
            Assert.fail("O item " + lastCustomCode + " não estava acessível");
            closeWindow();
        }
        Utils.criarArquivoUltimoID(bkpId);
    }

    // faz a pesquisa e preenchimento dos dados e realiza a insercao
    protected void inserirSuprimentos() throws Exception {
        String bkpId = null;
        try {
            String lastCustomCode = Utils.lerArquivo("massaDeDados", "last_custom_code");
            String lastQuantity = Utils.lerArquivo("massaDeDados", "last_quantity");
            bkpId = Utils.lerArquivo("ultimoItemInserido", "last_id");

            if (lastCustomCode != null) {
                pesquisaItemEPreencheDadosDaInsercao(lastCustomCode, lastQuantity, bkpId);

            } else {
                Utils.criarArquivoUltimoID(bkpId);
                Selenide.closeWindow();
                Assert.fail("Ocorreu um erro!");
            }
        } catch (Exception e) {
            Utils.criarArquivoUltimoID(bkpId);
            throw new Exception(e.getMessage());
        }
    }

    // faz consulta na API se tiver mais itens faz a insercao até nao tiver mais
    public void novaInsercao() throws Exception {
        String bkpId = Utils.lerArquivo("ultimoItemInserido", "last_id");
        try {

            while (true) {
                JsonPath jsonPath = Utils.consultarMassaDeDados(START_DATE, END_DATE, bkpId);

                if (jsonPathIsNull(jsonPath)) {
                    Utils.criarArquivo(null, Utils.lerArquivo("massaDeDados", "last_id"), null);
                    closeWindow();
                    break;
                } else {
                    prepararDadosParaNovaInsercao(jsonPath);
                }
            }
        } catch (Exception e) {
            Utils.criarArquivoUltimoID(bkpId);
            throw new Exception(e.getMessage());
        }
    }

    // pega os dados da nova consulta e realiza a insercao atraves do pesquisaItemEPreencheDadosDaInsercao()
    private void prepararDadosParaNovaInsercao(JsonPath jsonPath) throws Exception {
        String customCode = jsonPath.getString("items[0].custom_code");
        String quantity = jsonPath.getString("items[0].quantity");
        String id = jsonPath.getString("items[0].id");

        pesquisaItemEPreencheDadosDaInsercao(customCode, quantity, id);
        Utils.criarArquivo(null, id, null);
        Utils.criarArquivoUltimoID(id);
    }



    public void inserirSuprimentosUpaMariaDirci() throws Exception {
        consultarDados();
        acessarESelecionarPaciente();
        inserirSuprimentos();
        novaInsercao();
    }
}
