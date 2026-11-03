/*==============================================================*/
/* DBMS name:      Microsoft SQL Server 2012                    */
/* Created on:     2/11/2026 20:34:08                           */
/*==============================================================*/

-- drop database EurekaBank;
-- create database EurekaBank;

if exists (select 1
   from sys.sysreferences r join sys.sysobjects o on (o.id = r.constid and o.type = 'F')
   where r.fkeyid = object_id('CUENTAS') and o.name = 'FK_CUENTAS_RELATIONS_USUARIOS')
alter table CUENTAS
   drop constraint FK_CUENTAS_RELATIONS_USUARIOS
go

if exists (select 1
   from sys.sysreferences r join sys.sysobjects o on (o.id = r.constid and o.type = 'F')
   where r.fkeyid = object_id('MOVIMIENTOS') and o.name = 'FK_MOVIMIEN_RELATIONS_USUARIOS')
alter table MOVIMIENTOS
   drop constraint FK_MOVIMIEN_RELATIONS_USUARIOS
go

if exists (select 1
   from sys.sysreferences r join sys.sysobjects o on (o.id = r.constid and o.type = 'F')
   where r.fkeyid = object_id('MOVIMIENTOS') and o.name = 'FK_MOVIMIEN_RELATIONS_CUENTAS')
alter table MOVIMIENTOS
   drop constraint FK_MOVIMIEN_RELATIONS_CUENTAS
go

if exists (select 1
   from sys.sysreferences r join sys.sysobjects o on (o.id = r.constid and o.type = 'F')
   where r.fkeyid = object_id('MOVIMIENTOS') and o.name = 'FK_MOVIMIEN_RELATIONS_MOVIMIEN')
alter table MOVIMIENTOS
   drop constraint FK_MOVIMIEN_RELATIONS_MOVIMIEN
go

if exists (select 1
   from sys.sysreferences r join sys.sysobjects o on (o.id = r.constid and o.type = 'F')
   where r.fkeyid = object_id('PERFILES') and o.name = 'FK_PERFILES_RELATIONS_USUARIOS')
alter table PERFILES
   drop constraint FK_PERFILES_RELATIONS_USUARIOS
go

if exists (select 1
   from sys.sysreferences r join sys.sysobjects o on (o.id = r.constid and o.type = 'F')
   where r.fkeyid = object_id('USUARIOS') and o.name = 'FK_USUARIOS_RELATIONS_USUARIO_')
alter table USUARIOS
   drop constraint FK_USUARIOS_RELATIONS_USUARIO_
go

if exists (select 1
            from  sysobjects
           where  id = object_id('CUENTAS')
            and   type = 'U')
   drop table CUENTAS
go

if exists (select 1
            from  sysobjects
           where  id = object_id('MOVIMIENTOS')
            and   type = 'U')
   drop table MOVIMIENTOS
go

if exists (select 1
            from  sysobjects
           where  id = object_id('MOVIMIENTO_TIPOS')
            and   type = 'U')
   drop table MOVIMIENTO_TIPOS
go

if exists (select 1
            from  sysobjects
           where  id = object_id('PERFILES')
            and   type = 'U')
   drop table PERFILES
go

if exists (select 1
            from  sysobjects
           where  id = object_id('USUARIOS')
            and   type = 'U')
   drop table USUARIOS
go

if exists (select 1
            from  sysobjects
           where  id = object_id('USUARIO_TIPOS')
            and   type = 'U')
   drop table USUARIO_TIPOS
go

/*==============================================================*/
/* Table: CUENTAS                                               */
/*==============================================================*/
create table CUENTAS (
   ID_CUENTA            varchar(30)          not null,
   ID_EMPLEADO_CREADOR  int                  null,
   FECHA_CREACION_CUENTA datetime             null,
   SALDO_CUENTA         double precision     null,
   constraint PK_CUENTAS primary key nonclustered (ID_CUENTA)
)
go

if exists (select 1 from  sys.extended_properties
           where major_id = object_id('CUENTAS') and minor_id = 0)
begin 
   declare @CurrentUser sysname 
select @CurrentUser = user_name() 
execute sp_dropextendedproperty 'MS_Description',  
   'user', @CurrentUser, 'table', 'CUENTAS' 
 
end 


select @CurrentUser = user_name() 
execute sp_addextendedproperty 'MS_Description',  
   'Unicamente entran aqu� los clientes.
   
   El identificador es el n�mero de cuenta, el cual es el nombre de usuario del perfil.', 
   'user', @CurrentUser, 'table', 'CUENTAS'
go

/*==============================================================*/
/* Table: MOVIMIENTOS                                           */
/*==============================================================*/
create table MOVIMIENTOS (
   ID_MOVIMIENTO        int                  identity,
   ID_TIPO_MOVIMIENTO   int                  null,
   CUENTA_ORIGEN_MOVIMIENTO varchar(30)          null,
   CUENTA_DESTINO_MOVIMIENTO varchar(30)          null,
   FECHA_MOVIMIENTO     datetime             null,
   VALOR_MOVIMIENTO     double precision     null,
   constraint PK_MOVIMIENTOS primary key nonclustered (ID_MOVIMIENTO)
)
go

/*==============================================================*/
/* Table: MOVIMIENTO_TIPOS                                      */
/*==============================================================*/
create table MOVIMIENTO_TIPOS (
   ID_TIPO_MOVIMIENTO   int                  identity,
   NOMBRE_TIPO_MOVIMIENTO varchar(20)          null,
   DESCRIPCION_TIPO_MOVIMIENTO text                 null,
   ACCION_TIPO_MOVIMIENTO varchar(8)           null,
   constraint PK_MOVIMIENTO_TIPOS primary key nonclustered (ID_TIPO_MOVIMIENTO)
)
go

/*==============================================================*/
/* Table: PERFILES                                              */
/*==============================================================*/
create table PERFILES (
   ID_PERFIL            varchar(30)          not null,
   USERNAME_PERFIL      varchar(1024)        null,
   PASSWORD_PERFIL      varchar(1024)        null,
   constraint PK_PERFILES primary key nonclustered (ID_PERFIL),
   constraint AK_USERNAME_INDEX_PERFILES unique (USERNAME_PERFIL)
)
go

/*==============================================================*/
/* Table: USUARIOS                                              */
/*==============================================================*/
create table USUARIOS (
   ID_USUARIO           varchar(30)          not null,
   ID_TIPO_USUARIO      int                  null,
   NOMBRE_USUARIO       varchar(1024)        null,
   APELLIDO_USUARIO     varchar(1024)        null,
   ESTADO_USUARIO       varchar(1024)        null,
   DIRECCION_USUARIO    varchar(1024)        null,
   DNI_USUARIO          varchar(20)          null,
   constraint PK_USUARIOS primary key nonclustered (ID_USUARIO),
   constraint AK_DNI_INDEX_USUARIOS unique (DNI_USUARIO)
)
go

/*==============================================================*/
/* Table: USUARIO_TIPOS                                         */
/*==============================================================*/
create table USUARIO_TIPOS (
   ID_TIPO_USUARIO      int                  identity,
   NOMBRE_TIPO_USUARIO  varchar(1024)        null,
   DESCRIPCION_TIPO_USUARIO text                 null,
   ESTADO_TIPO_USUARIO  bit                  null,
   constraint PK_USUARIO_TIPOS primary key nonclustered (ID_TIPO_USUARIO)
)
go

alter table CUENTAS
   add constraint FK_CUENTAS_RELATIONS_USUARIOS foreign key (ID_CUENTA)
      references USUARIOS (ID_USUARIO)
go

alter table MOVIMIENTOS
   add constraint FK_MOVIMIEN_RELATIONS_USUARIOS foreign key (CUENTA_ORIGEN_MOVIMIENTO)
      references USUARIOS (ID_USUARIO)
go

alter table MOVIMIENTOS
   add constraint FK_MOVIMIEN_RELATIONS_CUENTAS foreign key (CUENTA_DESTINO_MOVIMIENTO)
      references CUENTAS (ID_CUENTA)
go

alter table MOVIMIENTOS
   add constraint FK_MOVIMIEN_RELATIONS_MOVIMIEN foreign key (ID_TIPO_MOVIMIENTO)
      references MOVIMIENTO_TIPOS (ID_TIPO_MOVIMIENTO)
go

alter table PERFILES
   add constraint FK_PERFILES_RELATIONS_USUARIOS foreign key (ID_PERFIL)
      references USUARIOS (ID_USUARIO)
go

alter table USUARIOS
   add constraint FK_USUARIOS_RELATIONS_USUARIO_ foreign key (ID_TIPO_USUARIO)
      references USUARIO_TIPOS (ID_TIPO_USUARIO)
go

