package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Drop extends ApplicationAdapter {

    private Texture meteoro_1;
    private Texture meteoro_2;
    private Texture meteoro_3;
    private Texture meteoro_4;
    private Texture lagarta_PNG;
    private Texture background;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Rectangle lagarta;
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    private State state;
	private BitmapFont font;
    private static int pontos = 0;
    private Array<Texture> meteoros;


    @Override
    public void create() {
		state = State.RUN;

        meteoro_1 = new Texture(Gdx.files.internal("meteoro_1.png"));
        meteoro_2 = new Texture(Gdx.files.internal("meteoro_2.png"));
        meteoro_3 = new Texture(Gdx.files.internal("meteoro_3.png"));
        meteoro_4 = new Texture(Gdx.files.internal("meteoro_4.png"));
        lagarta_PNG = new Texture(Gdx.files.internal("lagarta.png"));
        meteoros = new Array<>();
        meteoros.add(meteoro_1);
        meteoros.add(meteoro_2);
        meteoros.add(meteoro_3);
        meteoros.add(meteoro_4);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();

        lagarta = new Rectangle();
        lagarta.x = 800 / 2 - 64 / 2;
        lagarta.y = -20;
        lagarta.width = 70;
        lagarta.height = 70;

        raindrops = new Array<Rectangle>();
            spawnRaindrop();
            spawnRaindrop();
            spawnRaindrop();
            spawnRaindrop();

		font = new BitmapFont();
    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800 - 64);
        raindrop.y = 800;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render() {

		if (Gdx.input.isKeyPressed(Input.Keys.P))
			pause();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        background = new Texture("fundo.png");
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
        batch.draw(background,0,0);
		batch.draw(lagarta_PNG, lagarta.x, lagarta.y);

        for(int i = 0 ; i < raindrops.size; i ++) {
            batch.draw(meteoros.get(i), raindrops.get(i).x, raindrops.get(i).y);
        }
            batch.end();

		switch (state) {
            case RUN:
                if (Gdx.input.isTouched()) {
                    Vector3 touchPos = new Vector3();
                    touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                    camera.unproject(touchPos);
                    lagarta.x = touchPos.x - 64 / 2;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                    lagarta.x -= 200 * Gdx.graphics.getDeltaTime();
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                    lagarta.x += 200 * Gdx.graphics.getDeltaTime();

                if (lagarta.x < 0){
                lagarta.x = 0;
                lagarta_PNG = new Texture(Gdx.files.internal("lagarta.png"));
                }

                if (lagarta.x > 780 - 64){
                    lagarta_PNG = new Texture(Gdx.files.internal("lagarta2.png"));
                    lagarta.x = 800 - 64;}

                if(raindrops.size < 4){
                    spawnRaindrop();
                }

                int i = 0;
                for (Iterator<Rectangle> it = raindrops.iterator(); it.hasNext(); ) {
                    Rectangle raindrop = it.next();
                    raindrop.y -= 100 * Gdx.graphics.getDeltaTime();
                    if (raindrop.y + 64 < -20){
                        raindrop.y = 400-64;
                        raindrop.x = MathUtils.random(0, 800 - 64);
                    }
                    if (raindrop.overlaps(lagarta)) {
                        if(i == 0 || i == 1){
                            pontos ++;

                        }else if(i == 2 || i == 3){
                           pontos --;
                        }
                        if(pontos == 15){
                            pause();

                        }
                        raindrop.x = MathUtils.random(0, 800 - 64);
                        raindrop.y = 450;
                        //it.remove();
                    }
                    i ++;
                }

                break;
            case PAUSE:
            	batch.begin();
				font.draw(batch, "FIM DE JOGO", 380, 250);
                batch.end();
				break;
        }
        batch.begin();
        font.draw(batch, String.valueOf(pontos), 10, 470);
        batch.end();

    }

	@Override
    public void pause() {
        this.state = State.PAUSE;
    }

    @Override
    public void resume() {
        this.state = State.RUN;
    }

	@Override
    public void dispose() {
        meteoro_1.dispose();
        meteoro_2.dispose();
        meteoro_3.dispose();
        meteoro_4.dispose();
        lagarta_PNG.dispose();
        batch.dispose();
    }

    public enum State {
        PAUSE,
        RUN,
    }
}
