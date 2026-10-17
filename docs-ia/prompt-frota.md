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


## 4. Prompt do Protocolo (Few-Shot)
Na mesma conversa do Gemini Pro, foi enviado o prompt do protocolo de comunicação, que define o formato JSON da rajada e da resposta, com 3 exemplos (*few-shot prompting*):

> A nossa interação será através de objetos JSON, quer para a rajada de 3 tiros, quer para
a correspondente resposta. Uma rajada tem o seguinte formato:
{
[ {"row": "A", "column": 5},
{"row": "C", "column": 10},
{"row": "F", "column" : 5} ]
}
A resposta a uma rajada é feita em conjunto e não tiro a tiro, como nos seguintes 3
exemplos:
• Uma barca ao fundo, um tiro numa nau e um tiro na água
{
"validShots": 3,
"sunkBoats": [ {"count": 1, "type": "Barca"} ],
"repeatedShots": 0,
"outsideShots": 0,
"hitsOnBoats": [ {"hits": 1, "type": "Nau"} ],
"missedShots": 1
}
• Um tiro fora do tabuleiro, um tiro repetido e um tiro na água (o único válido)
{
"validShots": 1,
"sunkBoats": [ ],
"repeatedShots": 1,
"outsideShots": 1,
"hitsOnBoats": [ ],
"missedShots": 1
}
• Um tiro numa nau, um tiro no galeão e uma caravela ao fundo
{
"validShots": 3,
"sunkBoats": [{"count": 1, "type": "Caravela"}],
"repeatedShots": 0,
"outsideShots": 0,
"hitsOnBoats": [{"hits": 1, "type": "Nau"}, {"hits": 1, "type": "Galeao"}],
"missedShots": 0
}
O campo “repeatedShots” indica o número de tiros em posições já atingidas em jogadas
anteriores, ou na própria jogada (em ambos os casos, uma péssima estratégia porque
desperdiça tiros, só admissível por distração do jogador). O campo “outsideShots” indica
o número de tiros em posições exteriores ao tabuleiro (ex.: B12, K4 ou L14), um erro
também só admissível por distração do jogador. O campo “missedShots” corresponde ao
número de tiros na água.
Sugiro que me mande agora várias rajadas de tiros (uma de cada vez) e eu devolvo-lhe
o JSON da resposta. Pare só quando tiver aprendido o protocolo de comunicação do
jogo.

## 5. Troca de Rajadas com o Jogo
Em cada ronda: o Gemini enviava uma rajada em JSON; a rajada era traduzida para o comando do jogo (por exemplo `rajada B2 E5 H8`); e o primeiro bloco JSON impresso pelo jogo (a resposta, sem o tabuleiro) era colado no Gemini.

### Rajada 1
- Gemini: `[{"row":"B","column":2},{"row":"E","column":5},{"row":"H","column":8}]`
- Comando: `rajada B2 E5 H8`
- Resposta do jogo:
```json
{"validShots":3,"sunkBoats":[],"repeatedShots":0,"outsideShots":0,"hitsOnBoats":[{"hits":1,"type":"Nau"}],"missedShots":2}
```

### Rajada 2
- Gemini: `[{"row":"B","column":1},{"row":"B","column":3},{"row":"A","column":2}]`
- Comando: `rajada B1 B3 A2`
- Resposta do jogo:
```json
{"validShots":3,"sunkBoats":[{"count":1,"type":"Barca"}],"repeatedShots":0,"outsideShots":0,"hitsOnBoats":[],"missedShots":2}
```

### Rajada 3
- Gemini: `[{"row":"E","column":4},{"row":"E","column":6},{"row":"D","column":5}]`
- Comando: `rajada E4 E6 D5`
- Resposta do jogo:
```json
{"validShots":3,"sunkBoats":[],"repeatedShots":0,"outsideShots":0,"hitsOnBoats":[{"hits":1,"type":"Nau"}],"missedShots":2}
```

### Rajada 4
- Gemini: `[{"row":"E","column":3},{"row":"E","column":7},{"row":"C","column":5}]`
- Comando: `rajada E3 E7 C5`
- Resposta do jogo:
```json
{"validShots":3,"sunkBoats":[{"count":1,"type":"Nau"}],"repeatedShots":0,"outsideShots":0,"hitsOnBoats":[{"hits":1,"type":"Galeao"}],"missedShots":1}
```

## 6. Conclusão do Gemini
Ao fim de 4 rajadas, o Gemini resumiu corretamente o protocolo:
- `hitsOnBoats` conta os acertos que não afundam o navio;
- `sunkBoats` recebe o navio quando o último segmento é atingido (esse tiro não conta em `hitsOnBoats`);
- `validShots` agrega os tiros válidos, sem repetidos (`repeatedShots`) nem fora da grelha (`outsideShots`).

## 7. Observações
- O JSON da resposta é agregado: não diz qual dos 3 tiros acertou, por isso o LLM tem de o deduzir. Na rajada 2 assumiu que o acerto da rajada 1 tinha sido no B2, o que era falso (era água), e só mudou de hipótese depois de ver a resposta.
- A partir da rajada 3, o Gemini passou a explorar a linha E e afundou a Nau com o tiro em E7.
- Não houve tiros repetidos nem fora do tabuleiro, por isso esses dois campos ficaram sempre a 0.