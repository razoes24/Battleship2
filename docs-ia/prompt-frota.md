# Regras da Frota e Treino do LLM (Parte C)

## 1. Prompt Inicial
Foi utilizado o seguinte prompt para ensinar as regras da Batalha Naval dos Descobrimentos ao LLM (Gemini Pro):

> Considere que é um perito no famoso jogo da Batalha Naval, aqui numa versão do tempo dos Descobrimentos Portugueses, jogado num tabuleiro com linhas identificadas de A a J e colunas de 1 a 10. Deve começar por criar secretamente a sua frota de 11 navios:
> - 4 Barcas (1 posição na quadrícula)
> - 3 Caravelas (2 posições na quadrícula)
> - 2 Naus (3 posições na quadrícula)
> - 1 Fragata (4 posições na quadrícula)
> - 1 Galeão (5 posições na quadrícula, em forma de T, com um corpo de 3 posições e, numa das suas extremidades, uma posição adicional para cada lado, correspondentes às chamadas "asas da ponte")
>
> Os navios podem ser gerados com qualquer orientação no tabuleiro, desde que não toquem uns nos outros, nem mesmo por um canto (i.e. na diagonal), embora possam estar encostados às margens do tabuleiro. Repare que enquanto uma caravela, uma nau ou uma fragata pode ter duas orientações possíveis (norte-sul ou este-oeste), um galeão pode ter quatro orientações diferentes, consoante o braço do T esteja virado para norte, sul, este ou oeste. Já a questão da orientação não se coloca para uma barca, porque ocupa apenas uma posição no tabuleiro.

## 2. Pedidos de Verificação
Para garantir que o LLM compreendeu as regras (especialmente a forma do Galeão e o espaçamento entre navios), foram feitos os seguintes pedidos sequenciais:
1. "Mostre-me um tabuleiro criado com estas regras, com o Galeão virado a Norte. Deixe as posições da água em branco para melhor visualização das posições dos navios."
2. "Repete com Sul, Este e Oeste."

## 3. Resultados da Validação
O LLM passou em todos os critérios da checklist à 1.ª tentativa em todos os tabuleiros:
- **Galeão a Norte:** Correto à 1.ª tentativa.
- **Galeão a Sul:** Correto à 1.ª tentativa.
- **Galeão a Este:** Correto à 1.ª tentativa.
- **Galeão a Oeste:** Correto à 1.ª tentativa.

**Checklist confirmada em todos os testes:**
- [x] São 11 navios no total.
- [x] Total de 25 células ocupadas.
- [x] O Galeão tem forma de T e respeita a orientação pedida.
- [x] Nenhum navio toca noutro (ortogonal ou diagonal).
- [x] Respeita os limites A-J e 1-10.