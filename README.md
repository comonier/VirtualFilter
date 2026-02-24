<div align="center">

# 💎 VirtualFilter v1.5
**Professional Independent Virtual Filtering System**

An advanced item management system designed for high-performance Minecraft servers (1.21.1+).

---

## 🛡️ Chest Guard System (v1.5)
**Este sistema impede a perda de itens ao quebrar baús ou recipientes. Quando um baú é destruído o plugin processa os itens em três etapas primeiro tenta enviar para o estoque virtual ISF depois para o inventário do jogador e se não houver espaço dropa o restante no chão com total segurança.**

## 📊 Relatório Detalhado de Coleta
**Sempre que um baú é quebrado o jogador recebe um log colorido no chat informando o destino exato de cada item. É possível ver quantos itens foram para o ISF quantos entraram no inventário e se algo foi dropado por falta de espaço. Esta função pode ser ativada ou desativada via comando.**

## 🏗️ Construção Infinita com ISF
**O sistema AutoFillHand está integrado ao banco de dados. Se o bloco na sua mão acabar enquanto você constrói o plugin busca automaticamente um novo pack de 64 unidades direto do seu armazenamento virtual permitindo construções sem interrupções.**

## 📱 Compatibilidade Total Bedrock
**Desenvolvido para funcionar perfeitamente com GeyserMC. Jogadores de celular e console podem gerenciar filtros adicionar itens remover por ID de slot e sacar do estoque usando comandos curtos sem depender de cliques em menus que podem falhar.**

---

## 💻 Comandos do Jogador


| Comando | Descrição |
| :---: | :--- |
| `/vf` ou `/vfilter` | **Abre o menu principal de ajuda com todos os comandos.** |
| `/isg <slot> <quantia|all>` | **Saca itens do estoque virtual ISF usando o número do slot.** |
| `/addasf /addisf /addabf` | **Adiciona o item da mão aos filtros de Venda, Estoque ou Bloqueio.** |
| `/remasf /remisf /remabf` | **Remove o filtro do item da mão ou de um slot específico.** |
| `/al` | **Ativa ou desativa o recolhimento automático de itens (AutoLoot).** |
| `/afh` | **Ativa ou desativa a reposição automática de blocos (AutoFill).** |
| `/vfcb` ou `/chestdebug` | **Ativa ou desativa as mensagens de relatório ao quebrar baús.** |
| `/vflang <en|pt>` | **Altera o idioma das mensagens do plugin para o jogador.** |
| `/vfreload` | **Recarrega as configurações e preços do plugin (Admin).** |

---

## 🔑 Permissões do Sistema

* **virtualfilter.admin** - **Acesso total aos comandos de administração e reload.**
* **virtualfilter.chestdebug** - **Permite ao jogador ver e alternar as mensagens de baús.**
* **virtualfilter.asf.<numero>** - **Define slots disponíveis no filtro de AutoSell.**
* **virtualfilter.isf.<numero>** - **Define slots disponíveis no filtro de InfinityStack.**
* **virtualfilter.abf.<numero>** - **Define slots disponíveis no filtro de AutoBlock.**

---
*Developed by **comonier**.*

</div>
