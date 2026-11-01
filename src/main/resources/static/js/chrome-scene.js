/* Native WebGL metallic forms. Self hosted, zero packages, no external network calls.
   Decorative only: calculator and user authentication remain Java / Spring powered. */
(() => {
  'use strict';
  const canvas = document.getElementById('chromeCanvas');
  if (!canvas || !('WebGLRenderingContext' in window)) return;
  if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
  const gl = canvas.getContext('webgl', {alpha: true, antialias: true, premultipliedAlpha: true, powerPreference: 'low-power'});
  if (!gl) return;
  const vert = `
    attribute vec3 aPosition;
    attribute vec3 aNormal;
    uniform vec3 uRotate;
    uniform vec3 uTranslate;
    uniform float uScale;
    uniform float uAspect;
    varying vec3 vNormal;
    varying vec3 vPosition;
    void main(){
      vec3 p = aPosition * uScale;
      vec3 n = aNormal;
      float cx=cos(uRotate.x), sx=sin(uRotate.x), cy=cos(uRotate.y), sy=sin(uRotate.y), cz=cos(uRotate.z), sz=sin(uRotate.z);
      mat3 rx=mat3(1.,0.,0., 0.,cx,sx, 0.,-sx,cx);
      mat3 ry=mat3(cy,0.,-sy, 0.,1.,0., sy,0.,cy);
      mat3 rz=mat3(cz,sz,0., -sz,cz,0., 0.,0.,1.);
      mat3 rot=rz*ry*rx;
      p=rot*p+uTranslate;
      vNormal=normalize(rot*n);
      vPosition=p;
      float depth=max(1.0,5.3-p.z);
      gl_Position=vec4(p.x*3.3/(depth*uAspect),p.y*3.3/depth,(depth-5.0)/9.0,1.0);
    }`;
  const frag = `
    precision highp float;
    varying vec3 vNormal;
    varying vec3 vPosition;
    uniform float uTime;
    void main(){
      vec3 n=normalize(vNormal);
      vec3 v=normalize(vec3(0.,0.,6.5)-vPosition);
      vec3 r=reflect(-v,n);
      float studio1=pow(max(0.,dot(n,normalize(vec3(-.65,.95,1.2)))),13.0);
      float studio2=pow(max(0.,dot(n,normalize(vec3(.95,-.12,.8)))),22.0);
      float sweep=pow(max(0.,dot(n,normalize(vec3(.05,-.95,1.0)))),9.0);
      float strip=pow(max(0.,sin(r.x*7.0+r.y*5.0+r.z*2.2)),9.0);
      float dark=pow(max(0.,cos(r.y*8.0-r.x*3.8)),8.0);
      float fresnel=pow(1.0-max(0.,dot(n,v)),2.8);
      float shade=0.055+0.48*max(0.,dot(n,normalize(vec3(-.1,.28,.85))));
      vec3 col=vec3(.52,.54,.59)*shade;
      col+=vec3(.98,.99,1.0)*(studio1*.86+studio2*.70+sweep*.33+strip*.52);
      col+=vec3(.55,.58,.66)*fresnel*.8;
      col-=vec3(.18,.19,.22)*dark;
      col=mix(col,vec3(1.),clamp(studio1*studio2*2.,0.,.35));
      col=pow(max(col,vec3(0.)),vec3(.88));
      gl_FragColor=vec4(clamp(col,vec3(.03),vec3(1.)),.96);
    }`;
  function compile(type,src){const shader=gl.createShader(type);gl.shaderSource(shader,src);gl.compileShader(shader);if(!gl.getShaderParameter(shader,gl.COMPILE_STATUS)){console.warn('VOLTLEDGER background shader unavailable');gl.deleteShader(shader);return null;}return shader;}
  const v=compile(gl.VERTEX_SHADER,vert), f=compile(gl.FRAGMENT_SHADER,frag);
  if(!v||!f)return;
  const program=gl.createProgram();gl.attachShader(program,v);gl.attachShader(program,f);gl.linkProgram(program);
  if(!gl.getProgramParameter(program,gl.LINK_STATUS))return;
  const loc={pos:gl.getAttribLocation(program,'aPosition'),normal:gl.getAttribLocation(program,'aNormal'),rot:gl.getUniformLocation(program,'uRotate'),move:gl.getUniformLocation(program,'uTranslate'),scale:gl.getUniformLocation(program,'uScale'),aspect:gl.getUniformLocation(program,'uAspect'),time:gl.getUniformLocation(program,'uTime')};
  const P=2,Q=3,N=256,M=24,positions=[],normals=[],indices=[];
  const center=t=>{let r=1.3+.56*Math.cos(Q*t);return [r*Math.cos(P*t),r*Math.sin(P*t),.56*Math.sin(Q*t)];};
  const norm=a=>{let q=Math.hypot(...a)||1;return a.map(x=>x/q)};
  const cross=(a,b)=>[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]];
  for(let i=0;i<=N;i++){
    const t=(i/N)*Math.PI*2,c=center(t),p=center(t+.003),m=center(t-.003);
    const tangent=norm(p.map((z,k)=>z-m[k]));
    let outward=norm([Math.cos(P*t),Math.sin(P*t),Math.sin(Q*t)*.4]);
    let side=norm(cross(tangent,outward));outward=norm(cross(side,tangent));
    for(let j=0;j<=M;j++){
      let a=2*Math.PI*j/M;
      // Undulating thickness produces the liquid-chrome ribbon look.
      const width=.21*(1+.26*Math.sin(t*9+1.2));
      const nrm=norm(outward.map((x,k)=>x*Math.cos(a)+side[k]*Math.sin(a)));
      const pos=c.map((x,k)=>x+width*nrm[k]);
      positions.push(...pos);normals.push(...nrm);
      if(i<N&&j<M){let b=i*(M+1)+j,d=b+M+1;indices.push(b,b+1,d,b+1,d+1,d);}
    }
  }
  const vb=gl.createBuffer();gl.bindBuffer(gl.ARRAY_BUFFER,vb);gl.bufferData(gl.ARRAY_BUFFER,new Float32Array(positions),gl.STATIC_DRAW);
  const nb=gl.createBuffer();gl.bindBuffer(gl.ARRAY_BUFFER,nb);gl.bufferData(gl.ARRAY_BUFFER,new Float32Array(normals),gl.STATIC_DRAW);
  // WebGL 1 supports uint16 indices, the mesh has fewer than 65536 vertices.
  const ib=gl.createBuffer();gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,ib);gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,new Uint16Array(indices),gl.STATIC_DRAW);
  canvas.parentElement.classList.add("webgl-ready");
  gl.enable(gl.DEPTH_TEST);gl.depthFunc(gl.LEQUAL);gl.enable(gl.BLEND);gl.blendFunc(gl.SRC_ALPHA,gl.ONE_MINUS_SRC_ALPHA);
  const shapes=[{move:[1.6,.1,0],scale:1.18,rate:.075,tilt:.5},{move:[-3.0,-2.2,-1.5],scale:.76,rate:-.053,tilt:1.3},{move:[3.3,2.15,-2.2],scale:.65,rate:.045,tilt:.8}];
  let active=true,raf=0,frame=0,start=performance.now(),last=0;
  function resize(){let d=Math.min(window.devicePixelRatio||1,1.5);let w=Math.floor(canvas.clientWidth*d),h=Math.floor(canvas.clientHeight*d);if(w>0&&h>0&&(w!==canvas.width||h!==canvas.height)){canvas.width=w;canvas.height=h;gl.viewport(0,0,w,h);}}
  function render(now){raf=0;if(!active||document.hidden)return;if(now-last<33){raf=requestAnimationFrame(render);return;}last=now;resize();const secs=(now-start)*.001;gl.clearColor(0,0,0,0);gl.clear(gl.COLOR_BUFFER_BIT|gl.DEPTH_BUFFER_BIT);gl.useProgram(program);gl.uniform1f(loc.aspect,Math.max(.5,canvas.width/canvas.height));gl.uniform1f(loc.time,secs);
    gl.bindBuffer(gl.ARRAY_BUFFER,vb);gl.enableVertexAttribArray(loc.pos);gl.vertexAttribPointer(loc.pos,3,gl.FLOAT,false,0,0);
    gl.bindBuffer(gl.ARRAY_BUFFER,nb);gl.enableVertexAttribArray(loc.normal);gl.vertexAttribPointer(loc.normal,3,gl.FLOAT,false,0,0);gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,ib);
    const small=window.innerWidth<650;
    shapes.forEach((s,i)=>{
      const x=small? [0,-2.4,2.8][i]:s.move[0];
      const y=small? [-.8,-2.3,2.5][i]:s.move[1];
      gl.uniform3f(loc.move,x,y+Math.sin(secs*.23+i)*.1,s.move[2]);
      gl.uniform3f(loc.rot,s.tilt+secs*s.rate,secs*s.rate*.85+(i*1.2),secs*s.rate*.3);
      gl.uniform1f(loc.scale,s.scale*(small?.77:1));gl.drawElements(gl.TRIANGLES,indices.length,gl.UNSIGNED_SHORT,0);
    });
    raf=requestAnimationFrame(render);
  }
  function startRender(){if(!raf&&active&&!document.hidden)raf=requestAnimationFrame(render);}
  document.addEventListener('visibilitychange',()=>{if(document.hidden){if(raf)cancelAnimationFrame(raf);raf=0;}else startRender();});
  canvas.addEventListener('webglcontextlost',e=>{e.preventDefault();active=false;if(raf)cancelAnimationFrame(raf);raf=0;});
  window.addEventListener('resize',resize,{passive:true});
  startRender();
})();
