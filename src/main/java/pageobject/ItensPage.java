package pageobject;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.testng.Assert;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

public class ItensPage {
    SelenideElement txtPesquisaItem    = $(By.id("cphBody_Conteudo_txtProduto"));
    SelenideElement txtNovaPesquisa    = $(By.id("cphBody_Conteudo_txtPesquisa"));
    SelenideElement btnCancelaPesquisa = $(By.id("cphBody_Conteudo_btnLimpaBuscaPesquisa"));
    SelenideElement btnLimpaBusca      = $(By.id("cphBody_Conteudo_btnLimpaBusca"));
    public SelenideElement btnConfirmar       = $(By.id("cphBody_Conteudo_btnConfirmar"));
    SelenideElement txtQtdItens        = $(By.id("cphBody_Conteudo_txtQuantidade"));
    SelenideElement optListaItens      = $(By.xpath("/html/body/form/div[6]/div[3]/div/div[2]/div[2]/div/table/tbody/tr[1]"));
    SelenideElement modalConfirmacao   = $(By.id("cphBody_Conteudo_Msg_lblMensagem"));
    SelenideElement btnFechaModal      = $(By.id("fechaPopup"));
    SelenideElement btnVoltar          = $(By.id("cphBody_lbtVoltar"));
    SelenideElement lblmsgSucesso      = $(By.id("cphBody_Conteudo_Msg_lblMensagem"));

    public void pesquisarItens(String codigo){
        txtPesquisaItem.setValue(codigo);
        txtPesquisaItem.pressEnter();
    }

    //pesquisa novamente o item para contornar o problema na busca por item com menos de 3 caracteres
    public void realizarNovaPesquisaItem(String codItem){
        btnCancelaPesquisa.isEnabled();
        btnCancelaPesquisa.click();
        txtNovaPesquisa.setValue(codItem);
        txtNovaPesquisa.pressEnter();

    }

    public void inserirItens(String qtdd)  {
        txtQtdItens.isEnabled();
        txtQtdItens.setValue(qtdd);
        optListaItens.click();
        btnConfirmar.isEnabled();
        btnConfirmar.click();
    }

    public void verificarMensagemSucesso() {
        if(lblmsgSucesso.getText().equals("Item lançado com sucesso!")){
            modalConfirmacao.isEnabled();
            sleep(2000);
            btnFechaModal.click();
        }else{
            Assert.fail("Ocorreu um erro ao inserir o item");
        }
    }

    public void novaInsercao(String codigo, String qtdd){
        btnLimpaBusca.isEnabled();
        btnLimpaBusca.click();
        pesquisarItens("ALTEPLASE");
        realizarNovaPesquisaItem(codigo);
        inserirItens(qtdd);
    }
}